package com.example.back.runs.service;

import com.example.back.auth.security.AuthContext;
import com.example.back.artifacts.service.RunArtifactService;
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
import com.example.back.common.logging.LogContext;
import com.example.back.datasets.entity.DatasetEntity;
import com.example.back.datasets.entity.DatasetSnapshotEntity;
import com.example.back.datasets.repository.DatasetRepository;
import com.example.back.datasets.repository.DatasetSnapshotRepository;
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
import java.io.PrintWriter;
import java.io.StringWriter;
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

    private static final String DEFAULT_ENGINE_VERSION = "python-execution-engine/0.3.0-alpha.1";
    private static final String PYTHON_EXECUTE_ENDPOINT = "/internal/runs/execute";

    private final RunRepository runRepository;
    private final RunSnapshotRepository runSnapshotRepository;
    private final StrategyFileRepository strategyFileRepository;
    private final DatasetRepository datasetRepository;
    private final DatasetSnapshotRepository datasetSnapshotRepository;
    private final BacktestTradeRepository backtestTradeRepository;
    private final BacktestEquityPointRepository backtestEquityPointRepository;
    private final PythonParserClient pythonParserClient;
    private final RunFailureStateService runFailureStateService;
    private final RunArtifactService runArtifactService;
    private final ObjectMapper objectMapper;

    @Transactional
    public Long createRun(CreateBacktestRunRequest request) {
        validateTimeRange(request);
        Long userId = AuthContext.requireUserId();
        StrategyFileEntity strategy = getValidatedStrategy(request.getStrategyId(), userId);
        RunEntity run = buildRunEntity(request, strategy, userId);
        RunEntity savedRun = runRepository.saveAndFlush(run);
        runSnapshotRepository.save(buildSnapshot(savedRun, request, strategy, userId));
        try (LogContext.BoundContext ignored = LogContext.bind(
                savedRun.getCorrelationId(),
                String.valueOf(savedRun.getId()))
        ) {
            log.info("Created run entity");
        }
        markQueued(savedRun.getId());
        return savedRun.getId();
    }

    public void executeRun(Long runId) {
        RunEntity run = findOwnedRun(runId);
        StrategyFileEntity strategy = getValidatedStrategy(run.getStrategyId(), run.getUserId());

        try (LogContext.BoundContext ignored = LogContext.bind(run.getCorrelationId(), String.valueOf(run.getId()))) {
            markRunning(runId);

            try {
                PythonRunExecuteResponse response = pythonParserClient.executeRun(buildPythonRequest(run, strategy));
                if (response == null) {
                    markFailed(
                            runId,
                            "Python execution returned empty response",
                            writeJson(buildEmptyPythonResponseDetails(run))
                    );
                    return;
                }
                if (!Boolean.TRUE.equals(response.getSuccess())) {
                    String errorMessage = resolvePythonErrorMessage(response);
                    log.warn("Python execution reported failed run: {}", errorMessage);
                    markFailed(runId, errorMessage, writeJson(buildPythonErrorDetails(run, response)));
                    return;
                }
                markSucceeded(runId, response);
            } catch (RuntimeException exception) {
                log.error("Run execution failed", exception);
                markFailed(
                        runId,
                        exception.getMessage(),
                        writeJson(buildJavaErrorDetails(run, exception))
                );
            }
        }
    }

    private void validateTimeRange(CreateBacktestRunRequest request) {
        if (!request.getFrom().isBefore(request.getTo())) {
            throw new BacktestValidationException("Field 'from' must be before 'to'");
        }
    }

    private StrategyFileEntity getValidatedStrategy(Long strategyId, Long userId) {
        StrategyFileEntity strategy = strategyFileRepository.findByIdAndUserId(strategyId, userId)
                .orElseThrow(() -> new BacktestResourceNotFoundException("Strategy not found: " + strategyId));
        if (strategy.getStatus() != StrategyFileEntity.StrategyStatus.VALID) {
            throw new BacktestValidationException(
                    "Strategy must be VALID before execution. Current status: " + strategy.getStatus()
            );
        }
        return strategy;
    }

    private RunEntity buildRunEntity(CreateBacktestRunRequest request, StrategyFileEntity strategy, Long userId) {
        RunEntity run = new RunEntity();
        run.setUserId(userId);
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
        run.setDatasetId(findDataset(request, userId).map(DatasetEntity::getId).orElse(null));
        run.setParamsJson(writeJson(buildStoredConfig(request)));
        run.setSummaryJson(null);
        run.setMetricsJson(null);
        run.setArtifactsJson(null);
        run.setErrorMessage(null);
        run.setErrorDetailsJson(null);
        run.setEngineVersion(DEFAULT_ENGINE_VERSION);
        run.setExecutionDurationMs(null);
        run.setStartedAt(null);
        run.setFinishedAt(null);
        return run;
    }

    private RunSnapshotEntity buildSnapshot(
            RunEntity run,
            CreateBacktestRunRequest request,
            StrategyFileEntity strategy,
            Long userId
    ) {
        Optional<DatasetEntity> dataset = findDataset(request, userId);

        RunSnapshotEntity snapshot = new RunSnapshotEntity();
        DatasetEntity datasetEntity = dataset.orElse(null);
        snapshot.setRunId(run.getId());
        snapshot.setStrategyVersion(resolveStrategyVersion(strategy));
        snapshot.setDatasetVersion(resolveDatasetVersion(datasetEntity));
        snapshot.setDatasetSnapshotId(resolveDatasetSnapshotId(datasetEntity));
        snapshot.setParamsSnapshotJson(writeJson(request.getParams()));
        snapshot.setExecutionConfigSnapshotJson(writeJson(buildExecutionConfigSnapshot(request)));
        snapshot.setMarketAssumptionsSnapshotJson(writeJson(buildMarketAssumptionsSnapshot(request)));
        snapshot.setEngineVersion(DEFAULT_ENGINE_VERSION);
        return snapshot;
    }

    private Long resolveDatasetSnapshotId(DatasetEntity dataset) {
        if (dataset == null) {
            return null;
        }
        return datasetSnapshotRepository
                .findByDatasetIdAndDatasetVersion(dataset.getId(), resolveDatasetVersion(dataset))
                .or(() -> datasetSnapshotRepository.findFirstByDatasetIdOrderByCreatedAtDesc(dataset.getId()))
                .map(DatasetSnapshotEntity::getId)
                .orElse(null);
    }

    private Optional<DatasetEntity> findDataset(CreateBacktestRunRequest request, Long userId) {
        return datasetRepository
                .findFirstByUserIdAndSourceIgnoreCaseAndSymbolIgnoreCaseAndIntervalAndStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByImportedAtDesc(
                        userId,
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
        transitionStatus(run, BacktestStatus.QUEUED);
        runRepository.save(run);
    }

    @Transactional
    protected void markRunning(Long runId) {
        RunEntity run = findRun(runId);
        transitionStatus(run, BacktestStatus.RUNNING);
        run.setStartedAt(Instant.now());
        run.setFinishedAt(null);
        run.setExecutionDurationMs(null);
        run.setSummaryJson(null);
        run.setMetricsJson(null);
        run.setArtifactsJson(null);
        run.setErrorMessage(null);
        run.setErrorDetailsJson(null);
        runArtifactService.deleteArtifacts(runId);
        backtestTradeRepository.deleteByRunId(runId);
        backtestEquityPointRepository.deleteByRunId(runId);
        runRepository.save(run);
    }

    @Transactional
    protected void markSucceeded(Long runId, PythonRunExecuteResponse response) {
        RunEntity run = findRun(runId);
        transitionStatus(run, BacktestStatus.SUCCEEDED);
        validateResponseCorrelation(run, response);
        run.setFinishedAt(Instant.now());
        run.setExecutionDurationMs(resolveExecutionDurationMs(run.getStartedAt(), run.getFinishedAt()));
        run.setSummaryJson(writeJson(defaultMap(response.getSummary())));
        run.setMetricsJson(writeJson(defaultMap(response.getMetrics())));
        run.setArtifactsJson(writeJson(defaultMap(response.getArtifacts())));
        run.setErrorMessage(null);
        run.setErrorDetailsJson(null);
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
        runArtifactService.replaceArtifacts(
                runId,
                defaultMap(response.getSummary()),
                defaultMap(response.getMetrics()),
                defaultList(response.getTrades()),
                defaultList(response.getEquityCurve()),
                buildRunReport(run, response)
        );

        log.info("Run execution completed successfully in {} ms", run.getExecutionDurationMs());
    }

    private Map<String, Object> buildRunReport(RunEntity run, PythonRunExecuteResponse response) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("runId", run.getId());
        report.put("correlationId", run.getCorrelationId());
        report.put("status", run.getStatus());
        report.put("strategyId", run.getStrategyId());
        report.put("strategyName", run.getStrategyName());
        report.put("datasetId", run.getDatasetId());
        report.put("exchange", run.getExchange());
        report.put("symbol", run.getSymbol());
        report.put("interval", run.getInterval());
        report.put("from", run.getDateFrom());
        report.put("to", run.getDateTo());
        report.put("engineVersion", resolveEngineVersion(response.getEngineVersion()));
        report.put("executionDurationMs", run.getExecutionDurationMs());
        report.put("summary", defaultMap(response.getSummary()));
        report.put("metrics", defaultMap(response.getMetrics()));
        report.put("artifacts", defaultMap(response.getArtifacts()));
        return report;
    }

    private void markFailed(Long runId, String message, String errorDetailsJson) {
        try {
            runFailureStateService.markFailedInNewTransaction(runId, message, errorDetailsJson);
        } catch (RuntimeException ex) {
            log.error("Failed to persist FAILED status for run {}", runId, ex);
        }
    }

    private void transitionStatus(RunEntity run, BacktestStatus nextStatus) {
        try (LogContext.BoundContext ignored = LogContext.bind(run.getCorrelationId(), String.valueOf(run.getId()))) {
            BacktestStatus previousStatus = run.getStatus();
            run.setStatus(nextStatus);
            log.info("Run status transition {} -> {}", previousStatus, nextStatus);
        }
    }

    private void validateResponseCorrelation(RunEntity run, PythonRunExecuteResponse response) {
        String expectedRunId = String.valueOf(run.getId());
        if (response.getRunId() != null && !expectedRunId.equals(response.getRunId())) {
            log.warn("Python response returned mismatched run id: expected={}, actual={}", expectedRunId, response.getRunId());
        }
        if (response.getCorrelationId() != null && !run.getCorrelationId().equals(response.getCorrelationId())) {
            log.warn(
                    "Python response returned mismatched correlation id: expected={}, actual={}",
                    run.getCorrelationId(),
                    response.getCorrelationId()
            );
        }
    }

    private Map<String, Object> buildEmptyPythonResponseDetails(RunEntity run) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("source", "java");
        details.put("stage", "python-client");
        details.put("endpoint", PYTHON_EXECUTE_ENDPOINT);
        details.put("runId", run.getId());
        details.put("correlationId", run.getCorrelationId());
        details.put("errorCode", "EMPTY_PYTHON_RESPONSE");
        details.put("errorMessage", "Python execution returned empty response");
        return details;
    }

    private Map<String, Object> buildPythonErrorDetails(RunEntity run, PythonRunExecuteResponse response) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("source", "python");
        details.put("endpoint", PYTHON_EXECUTE_ENDPOINT);
        details.put("runId", firstNonBlank(response.getRunId(), String.valueOf(run.getId())));
        details.put("correlationId", firstNonBlank(response.getCorrelationId(), run.getCorrelationId()));
        details.put("errorCode", response.getErrorCode());
        details.put("errorMessage", resolvePythonErrorMessage(response));
        details.put("stacktrace", response.getStacktrace());
        details.put("startedAt", response.getStartedAt());
        details.put("finishedAt", response.getFinishedAt());
        details.put("executionDurationMs", response.getExecutionDurationMs());
        details.put("engineVersion", resolveEngineVersion(response.getEngineVersion()));
        return details;
    }

    private Map<String, Object> buildJavaErrorDetails(RunEntity run, RuntimeException exception) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("source", "java");
        details.put("stage", "python-client");
        details.put("endpoint", PYTHON_EXECUTE_ENDPOINT);
        details.put("runId", run.getId());
        details.put("correlationId", run.getCorrelationId());
        details.put("errorCode", "JAVA_EXECUTION_ERROR");
        details.put("errorMessage", firstNonBlank(exception.getMessage(), "Run execution failed"));
        details.put("exceptionClass", exception.getClass().getName());
        details.put("stacktrace", stackTrace(exception));
        return details;
    }

    private String resolvePythonErrorMessage(PythonRunExecuteResponse response) {
        return firstNonBlank(response.getErrorMessage(), response.getError());
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback;
    }

    private Long resolveExecutionDurationMs(Instant startedAt, Instant finishedAt) {
        if (startedAt == null || finishedAt == null) {
            return null;
        }
        return finishedAt.toEpochMilli() - startedAt.toEpochMilli();
    }

    private String stackTrace(Throwable throwable) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
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

    private RunEntity findOwnedRun(Long runId) {
        Long userId = AuthContext.requireUserId();
        return runRepository.findByIdAndUserId(runId, userId)
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
            return objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, Object.class)
            );
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
