package com.example.back.runs.service;

import com.example.back.backtest.dto.BacktestTrade;
import com.example.back.backtest.dto.CreateBacktestRunRequest;
import com.example.back.backtest.dto.EquityPoint;
import com.example.back.backtest.exception.BacktestResourceNotFoundException;
import com.example.back.backtest.exception.BacktestValidationException;
import com.example.back.backtest.model.BacktestEquityPointEntity;
import com.example.back.backtest.model.BacktestStatus;
import com.example.back.backtest.model.BacktestTradeEntity;
import com.example.back.backtest.repository.BacktestEquityPointRepository;
import com.example.back.backtest.repository.BacktestTradeRepository;
import com.example.back.datasets.entity.DatasetEntity;
import com.example.back.datasets.repository.DatasetRepository;
import com.example.back.imports.client.PythonParserClient;
import com.example.back.runs.dto.PythonRunExecuteRequest;
import com.example.back.runs.dto.PythonRunExecuteResponse;
import com.example.back.runs.entity.RunEntity;
import com.example.back.runs.entity.RunSnapshotEntity;
import com.example.back.runs.repository.RunRepository;
import com.example.back.runs.repository.RunSnapshotRepository;
import com.example.back.strategies.entity.StrategyFileEntity;
import com.example.back.strategies.repository.StrategyFileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RunOrchestrationService {

    private static final String DEFAULT_ENGINE_VERSION = "python-execution-engine/0.2.0-alpha";

    private final RunRepository runRepository;
    private final RunSnapshotRepository runSnapshotRepository;
    private final StrategyFileRepository strategyFileRepository;
    private final DatasetRepository datasetRepository;
    private final BacktestTradeRepository backtestTradeRepository;
    private final BacktestEquityPointRepository backtestEquityPointRepository;
    private final PythonParserClient pythonParserClient;
    private final RunFailureStateService runFailureStateService;
    private final ObjectMapper objectMapper;

    @Transactional
    public Long createRun(CreateBacktestRunRequest request) {
        validateTimeRange(request);
        StrategyFileEntity strategy = getValidatedStrategy(request.getStrategyId());
        RunEntity run = buildRunEntity(request, strategy);
        RunEntity savedRun = runRepository.saveAndFlush(run);
        runSnapshotRepository.save(buildSnapshot(savedRun, request, strategy));
        markQueued(savedRun.getId());
        return savedRun.getId();
    }

    public void executeRun(Long runId) {
        RunEntity run = findRun(runId);
        StrategyFileEntity strategy = getValidatedStrategy(run.getStrategyId());

        markRunning(runId);

        try {
            PythonRunExecuteResponse response = pythonParserClient.executeRun(buildPythonRequest(run, strategy));
            if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
                String error = response == null ? "Python execution returned empty response" : response.getError();
                markFailed(runId, error);
                return;
            }
            markSucceeded(runId, response);
        } catch (RuntimeException exception) {
            log.error("Run execution failed for runId={}: {}", runId, exception.getMessage(), exception);
            markFailed(runId, exception.getMessage());
        }
    }

    private void validateTimeRange(CreateBacktestRunRequest request) {
        if (!request.getFrom().isBefore(request.getTo())) {
            throw new BacktestValidationException("Field 'from' must be before 'to'");
        }
    }

    private StrategyFileEntity getValidatedStrategy(Long strategyId) {
        StrategyFileEntity strategy = strategyFileRepository.findById(strategyId)
                .orElseThrow(() -> new BacktestResourceNotFoundException("Strategy not found: " + strategyId));
        if (strategy.getStatus() != StrategyFileEntity.StrategyStatus.VALID) {
            throw new BacktestValidationException(
                    "Strategy must be VALID before execution. Current status: " + strategy.getStatus()
            );
        }
        return strategy;
    }

    private RunEntity buildRunEntity(CreateBacktestRunRequest request, StrategyFileEntity strategy) {
        RunEntity run = new RunEntity();
        run.setStrategyId(strategy.getId());
        run.setStrategyName(strategy.getName() == null || strategy.getName().isBlank()
                ? strategy.getFileName()
                : strategy.getName().trim());
        run.setRunName(resolveRunName(request, strategy));
        run.setCorrelationId("run-" + UUID.randomUUID());
        run.setStatus(BacktestStatus.CREATED);
        run.setExchange(request.getExchange().trim().toLowerCase());
        run.setSymbol(request.getSymbol().trim().toUpperCase());
        run.setInterval(request.getInterval().trim());
        run.setDateFrom(request.getFrom());
        run.setDateTo(request.getTo());
        run.setDatasetId(findDataset(request).map(DatasetEntity::getId).orElse(null));
        run.setParamsJson(writeJson(buildStoredConfig(request)));
        run.setSummaryJson(null);
        run.setMetricsJson(null);
        run.setArtifactsJson(null);
        run.setErrorMessage(null);
        run.setEngineVersion(DEFAULT_ENGINE_VERSION);
        run.setStartedAt(null);
        run.setFinishedAt(null);
        return run;
    }

    private RunSnapshotEntity buildSnapshot(
            RunEntity run,
            CreateBacktestRunRequest request,
            StrategyFileEntity strategy
    ) {
        Optional<DatasetEntity> dataset = findDataset(request);

        RunSnapshotEntity snapshot = new RunSnapshotEntity();
        snapshot.setRunId(run.getId());
        snapshot.setStrategyVersion(resolveStrategyVersion(strategy));
        snapshot.setDatasetVersion(resolveDatasetVersion(dataset.orElse(null)));
        snapshot.setParamsSnapshotJson(writeJson(request.getParams()));
        snapshot.setExecutionConfigSnapshotJson(writeJson(buildExecutionConfigSnapshot(request)));
        snapshot.setMarketAssumptionsSnapshotJson(writeJson(buildMarketAssumptionsSnapshot(request)));
        snapshot.setEngineVersion(DEFAULT_ENGINE_VERSION);
        return snapshot;
    }

    private Optional<DatasetEntity> findDataset(CreateBacktestRunRequest request) {
        return datasetRepository
                .findFirstBySourceIgnoreCaseAndSymbolIgnoreCaseAndIntervalAndStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByImportedAtDesc(
                        request.getExchange().trim(),
                        request.getSymbol().trim(),
                        request.getInterval().trim(),
                        request.getFrom(),
                        request.getTo()
                );
    }

    private String resolveRunName(CreateBacktestRunRequest request, StrategyFileEntity strategy) {
        if (request.getRunName() != null && !request.getRunName().isBlank()) {
            return request.getRunName().trim();
        }
        return "%s %s %s %s".formatted(
                strategy.getName() == null || strategy.getName().isBlank() ? strategy.getFileName() : strategy.getName(),
                request.getExchange().trim().toLowerCase(),
                request.getSymbol().trim().toUpperCase(),
                request.getInterval().trim()
        );
    }

    private String resolveStrategyVersion(StrategyFileEntity strategy) {
        Instant createdAt = strategy.getCreatedAt();
        if (createdAt == null) {
            return "strategy-%d".formatted(strategy.getId());
        }
        return "strategy-%d@%s".formatted(strategy.getId(), createdAt.toString());
    }

    private String resolveDatasetVersion(DatasetEntity dataset) {
        if (dataset == null) {
            return "untracked";
        }
        if (dataset.getVersion() != null && !dataset.getVersion().isBlank()) {
            return dataset.getVersion();
        }
        if (dataset.getFingerprint() != null && !dataset.getFingerprint().isBlank()) {
            return dataset.getFingerprint();
        }
        return dataset.getId();
    }

    private Map<String, Object> buildStoredConfig(CreateBacktestRunRequest request) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("strategyId", request.getStrategyId());
        config.put("runName", request.getRunName());
        config.put("exchange", request.getExchange().trim().toLowerCase());
        config.put("symbol", request.getSymbol().trim().toUpperCase());
        config.put("interval", request.getInterval().trim());
        config.put("from", request.getFrom());
        config.put("to", request.getTo());
        config.put("params", request.getParams());
        config.put("initialCash", request.getInitialCash());
        config.put("feeRate", request.getFeeRate());
        config.put("slippageBps", request.getSlippageBps());
        config.put("strictData", request.getStrictData());
        config.put("positionSizingMode", request.getPositionSizingMode());
        return config;
    }

    private Map<String, Object> buildExecutionConfigSnapshot(CreateBacktestRunRequest request) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("mode", "sync-http");
        config.put("strict_data", request.getStrictData());
        config.put("from", request.getFrom());
        config.put("to", request.getTo());
        config.put("engine_transport", "python-parser-http");
        return config;
    }

    private Map<String, Object> buildMarketAssumptionsSnapshot(CreateBacktestRunRequest request) {
        Map<String, Object> assumptions = new LinkedHashMap<>();
        assumptions.put("commission_bps", request.getFeeRate() == null ? 0.0 : request.getFeeRate() * 10_000.0);
        assumptions.put("slippage_bps", request.getSlippageBps());
        assumptions.put("initial_balance", request.getInitialCash());
        assumptions.put("position_sizing_mode", request.getPositionSizingMode());
        assumptions.put("exchange", request.getExchange().trim().toLowerCase());
        assumptions.put("symbol", request.getSymbol().trim().toUpperCase());
        assumptions.put("timeframe", request.getInterval().trim());
        return assumptions;
    }

    private PythonRunExecuteRequest buildPythonRequest(RunEntity run, StrategyFileEntity strategy) {
        Map<String, Object> config = readJsonMap(run.getParamsJson());
        Map<String, Object> params = Map.of();
        Object rawParams = config.get("params");
        if (rawParams instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> typedParams = (Map<String, Object>) map;
            params = typedParams;
        }

        PythonRunExecuteRequest request = new PythonRunExecuteRequest();
        request.setStrategyFilePath(strategy.getStoragePath());
        request.setExchange(run.getExchange());
        request.setSymbol(run.getSymbol());
        request.setInterval(run.getInterval());
        request.setFrom(run.getDateFrom().toString());
        request.setTo(run.getDateTo().toString());
        request.setParams(params);
        request.setRunId(String.valueOf(run.getId()));
        request.setCorrelationId(run.getCorrelationId());
        return request;
    }

    @Transactional
    protected void markQueued(Long runId) {
        RunEntity run = findRun(runId);
        run.setStatus(BacktestStatus.QUEUED);
        runRepository.save(run);
    }

    @Transactional
    protected void markRunning(Long runId) {
        RunEntity run = findRun(runId);
        run.setStatus(BacktestStatus.RUNNING);
        run.setStartedAt(Instant.now());
        run.setFinishedAt(null);
        run.setSummaryJson(null);
        run.setMetricsJson(null);
        run.setArtifactsJson(null);
        run.setErrorMessage(null);
        backtestTradeRepository.deleteByRunId(runId);
        backtestEquityPointRepository.deleteByRunId(runId);
        runRepository.save(run);
    }

    @Transactional
    protected void markSucceeded(Long runId, PythonRunExecuteResponse response) {
        RunEntity run = findRun(runId);
        run.setStatus(BacktestStatus.SUCCEEDED);
        run.setFinishedAt(Instant.now());
        run.setSummaryJson(writeJson(defaultMap(response.getSummary())));
        run.setMetricsJson(writeJson(defaultMap(response.getMetrics())));
        run.setArtifactsJson(writeJson(defaultMap(response.getArtifacts())));
        run.setErrorMessage(null);
        run.setEngineVersion(resolveEngineVersion(response.getEngineVersion()));
        runRepository.save(run);

        RunSnapshotEntity snapshot = runSnapshotRepository.findById(runId).orElse(null);
        if (snapshot != null) {
            snapshot.setEngineVersion(run.getEngineVersion());
            runSnapshotRepository.save(snapshot);
        }

        backtestTradeRepository.saveAll(defaultList(response.getTrades()).stream()
                .map(trade -> toTradeEntity(runId, trade))
                .toList());
        backtestEquityPointRepository.saveAll(defaultList(response.getEquityCurve()).stream()
                .map(point -> toEquityPointEntity(runId, point))
                .toList());
    }

    private void markFailed(Long runId, String message) {
        try {
            runFailureStateService.markFailedInNewTransaction(runId, message);
        } catch (RuntimeException ex) {
            log.error("Failed to persist FAILED status for run {}", runId, ex);
        }
    }

    private String resolveEngineVersion(String engineVersion) {
        if (engineVersion == null || engineVersion.isBlank()) {
            return DEFAULT_ENGINE_VERSION;
        }
        return engineVersion;
    }

    private RunEntity findRun(Long runId) {
        return runRepository.findById(runId)
                .orElseThrow(() -> new BacktestResourceNotFoundException("Run not found: " + runId));
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

    private Map<String, Object> readJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, Object.class));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to parse JSON payload", exception);
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize JSON payload", exception);
        }
    }

    private Map<String, Object> defaultMap(Map<String, Object> value) {
        return value == null ? Map.of() : value;
    }

    private <T> List<T> defaultList(List<T> value) {
        return value == null ? List.of() : value;
    }
}
