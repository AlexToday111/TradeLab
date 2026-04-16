package com.example.back.backtest.service;

import com.example.back.backtest.dto.BacktestRequest;
import com.example.back.backtest.dto.BacktestResult;
import com.example.back.backtest.dto.BacktestRunResponse;
import com.example.back.backtest.dto.BacktestTrade;
import com.example.back.backtest.dto.CreateBacktestRunRequest;
import com.example.back.backtest.dto.EquityPoint;
import com.example.back.backtest.exception.BacktestResourceNotFoundException;
import com.example.back.backtest.exception.BacktestValidationException;
import com.example.back.backtest.executor.PythonBacktestExecutor;
import com.example.back.backtest.model.BacktestEquityPointEntity;
import com.example.back.backtest.model.BacktestStatus;
import com.example.back.backtest.model.BacktestTradeEntity;
import com.example.back.backtest.repository.BacktestEquityPointRepository;
import com.example.back.backtest.repository.BacktestTradeRepository;
import com.example.back.candles.entity.CandleEntity;
import com.example.back.candles.repository.CandleRepository;
import com.example.back.datasets.service.DatasetService;
import com.example.back.runs.entity.RunEntity;
import com.example.back.runs.repository.RunRepository;
import com.example.back.runs.service.RunFailureStateService;
import com.example.back.runs.service.RunQueryService;
import com.example.back.strategies.entity.StrategyFileEntity;
import com.example.back.strategies.repository.StrategyFileRepository;
import com.example.back.telegram.service.TelegramNotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
public class BacktestService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final RunRepository runRepository;
    private final StrategyFileRepository strategyFileRepository;
    private final CandleRepository candleRepository;
    private final DatasetService datasetService;
    private final BacktestTradeRepository backtestTradeRepository;
    private final BacktestEquityPointRepository backtestEquityPointRepository;
    private final PythonBacktestExecutor pythonBacktestExecutor;
    private final RunFailureStateService runFailureStateService;
    private final RunQueryService runQueryService;
    private final TelegramNotificationService telegramNotificationService;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    public BacktestService(
            RunRepository runRepository,
            StrategyFileRepository strategyFileRepository,
            CandleRepository candleRepository,
            DatasetService datasetService,
            BacktestTradeRepository backtestTradeRepository,
            BacktestEquityPointRepository backtestEquityPointRepository,
            PythonBacktestExecutor pythonBacktestExecutor,
            RunFailureStateService runFailureStateService,
            RunQueryService runQueryService,
            TelegramNotificationService telegramNotificationService,
            ObjectMapper objectMapper,
            PlatformTransactionManager transactionManager
    ) {
        this.runRepository = runRepository;
        this.strategyFileRepository = strategyFileRepository;
        this.candleRepository = candleRepository;
        this.datasetService = datasetService;
        this.backtestTradeRepository = backtestTradeRepository;
        this.backtestEquityPointRepository = backtestEquityPointRepository;
        this.pythonBacktestExecutor = pythonBacktestExecutor;
        this.runFailureStateService = runFailureStateService;
        this.runQueryService = runQueryService;
        this.telegramNotificationService = telegramNotificationService;
        this.objectMapper = objectMapper;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public Long createRun(CreateBacktestRunRequest request) {
        validateTimeRange(request.getFrom(), request.getTo());
        StrategyFileEntity strategy = getValidatedStrategy(request.getStrategyId());

        RunEntity run = new RunEntity();
        run.setStrategyId(strategy.getId());
        run.setStrategyName(strategy.getName() == null || strategy.getName().isBlank()
                ? strategy.getFileName()
                : strategy.getName().trim());
        run.setCorrelationId("run-" + UUID.randomUUID());
        run.setStatus(BacktestStatus.PENDING);
        run.setExchange(request.getExchange().trim());
        run.setSymbol(request.getSymbol().trim());
        run.setInterval(request.getInterval().trim());
        run.setDateFrom(request.getFrom());
        run.setDateTo(request.getTo());
        run.setDatasetId(datasetService.findDatasetIdForRange(
                request.getExchange().trim(),
                request.getSymbol().trim(),
                request.getInterval().trim(),
                request.getFrom(),
                request.getTo()
        ).orElse(null));
        run.setParamsJson(writeJson(toStoredRequest(request)));
        run.setMetricsJson(null);
        run.setArtifactsJson(null);
        run.setErrorMessage(null);
        run.setStartedAt(null);
        run.setFinishedAt(null);

        return runRepository.save(run).getId();
    }

    public BacktestRunResponse executeRun(Long runId) {
        RunEntity run = findRunEntity(runId);
        if (run.getStatus() == BacktestStatus.RUNNING) {
            throw new BacktestValidationException("Backtest is already running");
        }

        StrategyFileEntity strategy = getValidatedStrategy(run.getStrategyId());
        StoredBacktestRequest storedRequest = readStoredRequest(run.getParamsJson());
        Path csvPath = null;

        markRunning(runId);
        sendRunStartedNotification(runId);

        try {
            List<CandleEntity> candles = loadCandles(run);
            csvPath = writeCandlesCsv(runId, candles);
            BacktestResult result = pythonBacktestExecutor.execute(toExecutorRequest(strategy, storedRequest, csvPath));
            persistSuccessfulRun(runId, result);
            sendRunCompletedNotification(runId);
            return getRun(runId);
        } catch (RuntimeException ex) {
            markFailed(runId, ex);
            throw ex;
        } catch (Exception ex) {
            markFailed(runId, ex);
            throw new IllegalStateException("Failed to execute backtest", ex);
        } finally {
            deleteQuietly(csvPath);
        }
    }

    public BacktestRunResponse getRun(Long runId) {
        RunEntity run = findRunEntity(runId);
        StoredBacktestRequest storedRequest = readStoredRequest(run.getParamsJson());

        return BacktestRunResponse.builder()
                .runId(run.getId())
                .strategyId(run.getStrategyId())
                .strategyName(run.getStrategyName())
                .datasetId(run.getDatasetId())
                .correlationId(run.getCorrelationId())
                .status(run.getStatus())
                .exchange(run.getExchange())
                .symbol(run.getSymbol())
                .interval(run.getInterval())
                .from(run.getDateFrom())
                .to(run.getDateTo())
                .params(storedRequest.params())
                .config(readJsonMap(run.getParamsJson()))
                .summary(readJsonMap(run.getMetricsJson()))
                .artifacts(readJsonMap(run.getArtifactsJson()))
                .errorMessage(run.getErrorMessage())
                .createdAt(run.getCreatedAt())
                .startedAt(run.getStartedAt())
                .finishedAt(run.getFinishedAt())
                .build();
    }

    public List<BacktestTrade> getTrades(Long runId) {
        findRunEntity(runId);
        return backtestTradeRepository.findByRunIdOrderByEntryTimeAsc(runId).stream()
                .map(this::toTradeDto)
                .toList();
    }

    public List<EquityPoint> getEquity(Long runId) {
        findRunEntity(runId);
        return backtestEquityPointRepository.findByRunIdOrderByTimestampAsc(runId).stream()
                .map(this::toEquityPointDto)
                .toList();
    }

    private void validateTimeRange(Instant from, Instant to) {
        if (!from.isBefore(to)) {
            throw new BacktestValidationException("Field 'from' must be before 'to'");
        }
    }

    private StrategyFileEntity getValidatedStrategy(Long strategyId) {
        StrategyFileEntity strategy = strategyFileRepository.findById(strategyId)
                .orElseThrow(() -> new BacktestResourceNotFoundException("Strategy not found: " + strategyId));
        if (strategy.getStatus() != StrategyFileEntity.StrategyStatus.VALID) {
            throw new BacktestValidationException(
                    "Strategy must be VALID before backtest execution. Current status: " + strategy.getStatus()
            );
        }
        return strategy;
    }

    private RunEntity findRunEntity(Long runId) {
        return runRepository.findById(runId)
                .orElseThrow(() -> new BacktestResourceNotFoundException("Run not found: " + runId));
    }

    private List<CandleEntity> loadCandles(RunEntity run) {
        List<CandleEntity> candles =
                candleRepository.findByExchangeAndSymbolAndIntervalAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualOrderByOpenTimeAsc(
                        run.getExchange(),
                        run.getSymbol(),
                        run.getInterval(),
                        run.getDateFrom(),
                        run.getDateTo()
                );
        if (candles.isEmpty()) {
            throw new BacktestValidationException("No candles found for requested range");
        }
        return candles;
    }

    private BacktestRequest toExecutorRequest(
            StrategyFileEntity strategy,
            StoredBacktestRequest storedRequest,
            Path csvPath
    ) {
        BacktestRequest request = new BacktestRequest();
        request.setStrategyPath(strategy.getStoragePath());
        request.setDataPath(csvPath.toString());
        request.setStrategyParams(storedRequest.params());
        request.setInitialCash(storedRequest.initialCash());
        request.setFeeRate(storedRequest.feeRate());
        request.setSlippageBps(storedRequest.slippageBps());
        request.setStrictData(storedRequest.strictData());
        return request;
    }

    private Path writeCandlesCsv(Long runId, List<CandleEntity> candles) throws IOException {
        Path csvPath = Files.createTempFile("backtest-run-" + runId + "-", ".csv");
        try (BufferedWriter writer = Files.newBufferedWriter(csvPath)) {
            writer.write("timestamp,open,high,low,close,volume");
            writer.newLine();
            for (CandleEntity candle : candles) {
                writer.write(toCsvLine(candle));
                writer.newLine();
            }
        }
        return csvPath;
    }

    private String toCsvLine(CandleEntity candle) {
        return String.join(
                ",",
                candle.getOpenTime().toString(),
                toPlainString(candle.getOpen()),
                toPlainString(candle.getHigh()),
                toPlainString(candle.getLow()),
                toPlainString(candle.getClose()),
                toPlainString(candle.getVolume())
        );
    }

    private String toPlainString(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private void markRunning(Long runId) {
        transactionTemplate.executeWithoutResult(status -> {
            RunEntity run = findRunEntity(runId);
            run.setStatus(BacktestStatus.RUNNING);
            run.setStartedAt(Instant.now());
            run.setFinishedAt(null);
            run.setMetricsJson(null);
            run.setArtifactsJson(null);
            run.setErrorMessage(null);
            backtestTradeRepository.deleteByRunId(runId);
            backtestEquityPointRepository.deleteByRunId(runId);
            runRepository.save(run);
        });
    }

    private void persistSuccessfulRun(Long runId, BacktestResult result) {
        transactionTemplate.executeWithoutResult(status -> {
            RunEntity run = findRunEntity(runId);
            run.setStatus(BacktestStatus.COMPLETED);
            run.setMetricsJson(writeJson(result.getSummary() == null ? Collections.emptyMap() : result.getSummary()));
            run.setArtifactsJson(writeJson(buildArtifactsManifest(result)));
            run.setErrorMessage(null);
            run.setFinishedAt(Instant.now());
            backtestTradeRepository.saveAll(safeList(result.getTrades()).stream()
                    .map(trade -> toTradeEntity(runId, trade))
                    .toList());
            backtestEquityPointRepository.saveAll(safeList(result.getEquityCurve()).stream()
                    .map(point -> toEquityPointEntity(runId, point))
                    .toList());
            runRepository.save(run);
        });
    }

    public Long rerun(Long runId) {
        RunEntity sourceRun = findRunEntity(runId);
        StoredBacktestRequest storedRequest = readStoredRequest(sourceRun.getParamsJson());

        CreateBacktestRunRequest request = new CreateBacktestRunRequest();
        request.setStrategyId(sourceRun.getStrategyId());
        request.setExchange(sourceRun.getExchange());
        request.setSymbol(sourceRun.getSymbol());
        request.setInterval(sourceRun.getInterval());
        request.setFrom(sourceRun.getDateFrom());
        request.setTo(sourceRun.getDateTo());
        request.setParams(storedRequest.params());
        request.setInitialCash(storedRequest.initialCash());
        request.setFeeRate(storedRequest.feeRate());
        request.setSlippageBps(storedRequest.slippageBps());
        request.setStrictData(storedRequest.strictData());

        return createRun(request);
    }

    private void markFailed(Long runId, Exception failure) {
        try {
            runFailureStateService.markFailedInNewTransaction(runId, failure.getMessage());
        } catch (RuntimeException ex) {
            failure.addSuppressed(ex);
            log.error("Failed to persist FAILED status for run {}", runId, ex);
        }
    }

    private void sendRunStartedNotification(Long runId) {
        runQueryService.findRun(runId)
                .ifPresent(telegramNotificationService::sendRunStarted);
    }

    private void sendRunCompletedNotification(Long runId) {
        runQueryService.findRun(runId)
                .ifPresent(telegramNotificationService::sendRunCompleted);
    }

    private BacktestTradeEntity toTradeEntity(Long runId, BacktestTrade trade) {
        BacktestTradeEntity entity = new BacktestTradeEntity();
        entity.setRunId(runId);
        entity.setEntryTime(trade.getEntryTime());
        entity.setExitTime(trade.getExitTime());
        entity.setEntryPrice(trade.getEntryPrice());
        entity.setExitPrice(trade.getExitPrice());
        entity.setQuantity(trade.getQuantity());
        entity.setPnl(trade.getPnl());
        entity.setFee(trade.getFee());
        return entity;
    }

    private BacktestEquityPointEntity toEquityPointEntity(Long runId, EquityPoint point) {
        BacktestEquityPointEntity entity = new BacktestEquityPointEntity();
        entity.setRunId(runId);
        entity.setTimestamp(point.getTimestamp());
        entity.setEquity(point.getEquity());
        return entity;
    }

    private BacktestTrade toTradeDto(BacktestTradeEntity entity) {
        BacktestTrade trade = new BacktestTrade();
        trade.setEntryTime(entity.getEntryTime());
        trade.setExitTime(entity.getExitTime());
        trade.setEntryPrice(entity.getEntryPrice());
        trade.setExitPrice(entity.getExitPrice());
        trade.setQuantity(entity.getQuantity());
        trade.setPnl(entity.getPnl());
        trade.setFee(entity.getFee());
        return trade;
    }

    private EquityPoint toEquityPointDto(BacktestEquityPointEntity entity) {
        EquityPoint point = new EquityPoint();
        point.setTimestamp(entity.getTimestamp());
        point.setEquity(entity.getEquity());
        return point;
    }

    private StoredBacktestRequest toStoredRequest(CreateBacktestRunRequest request) {
        return new StoredBacktestRequest(
                request.getStrategyId(),
                request.getExchange().trim(),
                request.getSymbol().trim(),
                request.getInterval().trim(),
                request.getFrom(),
                request.getTo(),
                new LinkedHashMap<>(request.getParams()),
                request.getInitialCash(),
                request.getFeeRate(),
                request.getSlippageBps(),
                request.getStrictData()
        );
    }

    private StoredBacktestRequest readStoredRequest(String json) {
        if (json == null || json.isBlank()) {
            return StoredBacktestRequest.empty();
        }

        try {
            JsonNode root = objectMapper.readTree(json);
            if (root.isObject() && root.has("strategyId")) {
                StoredBacktestRequest request = objectMapper.treeToValue(root, StoredBacktestRequest.class);
                return request == null ? StoredBacktestRequest.empty() : request.withDefaults();
            }
            if (root.isObject()) {
                Map<String, Object> params = objectMapper.convertValue(root, MAP_TYPE);
                return StoredBacktestRequest.empty().withParams(params);
            }
        } catch (Exception ex) {
            log.warn("Failed to parse stored run request JSON for backtest run", ex);
        }
        return StoredBacktestRequest.empty();
    }

    private Map<String, Object> readJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception ex) {
            log.warn("Failed to parse JSON payload: {}", json, ex);
            return Collections.emptyMap();
        }
    }

    private <T> List<T> safeList(List<T> value) {
        return value == null ? List.of() : value;
    }

    private Map<String, Object> buildArtifactsManifest(BacktestResult result) {
        List<BacktestTrade> trades = safeList(result.getTrades());
        List<EquityPoint> equityCurve = safeList(result.getEquityCurve());
        Map<String, Object> artifacts = new LinkedHashMap<>();
        artifacts.put("tradesCount", trades.size());
        artifacts.put("equityPointCount", equityCurve.size());
        artifacts.put("hasTrades", !trades.isEmpty());
        artifacts.put("hasEquityCurve", !equityCurve.isEmpty());
        artifacts.put("warnings", safeList(result.getWarnings()));
        artifacts.put("logs", safeList(result.getLogs()));
        return artifacts;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize JSON payload", ex);
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            log.warn("Failed to delete temp backtest CSV {}", path, ex);
        }
    }

    private record StoredBacktestRequest(
            Long strategyId,
            String exchange,
            String symbol,
            String interval,
            Instant from,
            Instant to,
            Map<String, Object> params,
            Double initialCash,
            Double feeRate,
            Double slippageBps,
            Boolean strictData
    ) {
        private static StoredBacktestRequest empty() {
            return new StoredBacktestRequest(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    new LinkedHashMap<>(),
                    10_000.0,
                    0.0,
                    0.0,
                    Boolean.TRUE
            );
        }

        private StoredBacktestRequest withDefaults() {
            return new StoredBacktestRequest(
                    strategyId,
                    exchange,
                    symbol,
                    interval,
                    from,
                    to,
                    params == null ? new LinkedHashMap<>() : new LinkedHashMap<>(params),
                    initialCash == null ? 10_000.0 : initialCash,
                    feeRate == null ? 0.0 : feeRate,
                    slippageBps == null ? 0.0 : slippageBps,
                    strictData == null ? Boolean.TRUE : strictData
            );
        }

        private StoredBacktestRequest withParams(Map<String, Object> params) {
            return new StoredBacktestRequest(
                    strategyId,
                    exchange,
                    symbol,
                    interval,
                    from,
                    to,
                    params == null ? new LinkedHashMap<>() : new LinkedHashMap<>(params),
                    initialCash,
                    feeRate,
                    slippageBps,
                    strictData
            ).withDefaults();
        }
    }
}
