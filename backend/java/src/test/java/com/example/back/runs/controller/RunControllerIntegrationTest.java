package com.example.back.runs.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.back.backtest.dto.BacktestResult;
import com.example.back.backtest.dto.BacktestTrade;
import com.example.back.backtest.dto.EquityPoint;
import com.example.back.backtest.executor.PythonBacktestExecutor;
import com.example.back.backtest.model.BacktestStatus;
import com.example.back.candles.entity.CandleEntity;
import com.example.back.candles.repository.CandleRepository;
import com.example.back.datasets.entity.DatasetEntity;
import com.example.back.datasets.repository.DatasetRepository;
import com.example.back.imports.client.PythonParserClient;
import com.example.back.runs.dto.PythonRunExecuteResponse;
import com.example.back.runs.entity.RunEntity;
import com.example.back.runs.repository.RunRepository;
import com.example.back.strategies.entity.StrategyFileEntity;
import com.example.back.strategies.repository.StrategyFileRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:runs-controller;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=INTERVAL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
class RunControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RunRepository runRepository;

    @Autowired
    private StrategyFileRepository strategyFileRepository;

    @Autowired
    private DatasetRepository datasetRepository;

    @MockBean
    private PythonBacktestExecutor pythonBacktestExecutor;

    @MockBean
    private PythonParserClient pythonParserClient;

    @MockBean
    private CandleRepository candleRepository;

    private Long strategyId;

    @BeforeEach
    void setUp() {
        runRepository.deleteAll();
        strategyFileRepository.deleteAll();
        datasetRepository.deleteAll();

        StrategyFileEntity strategy = new StrategyFileEntity();
        strategy.setName("EMA");
        strategy.setFileName("ema.py");
        strategy.setStoragePath("/tmp/ema.py");
        strategy.setStatus(StrategyFileEntity.StrategyStatus.VALID);
        strategyId = strategyFileRepository.saveAndFlush(strategy).getId();

        DatasetEntity dataset = new DatasetEntity();
        dataset.setId("dataset-1");
        dataset.setName("Binance BTCUSDT 1h");
        dataset.setSource("binance");
        dataset.setSymbol("BTCUSDT");
        dataset.setInterval("1h");
        dataset.setImportedAt(Instant.parse("2024-01-05T00:00:00Z"));
        dataset.setRowsCount(100);
        dataset.setStartAt(Instant.parse("2024-01-01T00:00:00Z"));
        dataset.setEndAt(Instant.parse("2024-01-03T00:00:00Z"));
        dataset.setVersion("abc123");
        dataset.setFingerprint("abc123");
        dataset.setQualityFlagsJson("[]");
        dataset.setLineageJson("{\"rawRows\":100}");
        dataset.setPayload("""
                {"id":"dataset-1","name":"Binance BTCUSDT 1h","source":"binance","symbol":"BTCUSDT","timeframe":"1h"}
                """);
        datasetRepository.saveAndFlush(dataset);
    }

    @Test
    void getRunsReturnsSortedListWithFrontendContract() throws Exception {
        RunEntity completedRun = saveRun(
                BacktestStatus.SUCCEEDED,
                Instant.parse("2024-01-01T00:00:00Z"),
                "{\"fastPeriod\":10}",
                "{\"profit\":9.5}",
                null
        );
        RunEntity failedRun = saveRun(
                BacktestStatus.FAILED,
                Instant.parse("2024-01-02T00:00:00Z"),
                "{\"slowPeriod\":21}",
                null,
                "boom"
        );

        mockMvc.perform(get("/api/runs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(failedRun.getId()))
                .andExpect(jsonPath("$[0].status").value("FAILED"))
                .andExpect(jsonPath("$[0].strategyName").value("EMA"))
                .andExpect(jsonPath("$[0].correlationId").isString())
                .andExpect(jsonPath("$[0].executionDurationMs").value(5000))
                .andExpect(jsonPath("$[1].id").value(completedRun.getId()))
                .andExpect(jsonPath("$[1].status").value("SUCCEEDED"))
                .andExpect(jsonPath("$[1].metrics.profit").value(9.5))
                .andExpect(jsonPath("$[1].parameters.fastPeriod").value(10))
                .andExpect(jsonPath("$[1].config.fastPeriod").value(10))
                .andExpect(jsonPath("$[1].runId").doesNotExist())
                .andExpect(jsonPath("$[1].summary").doesNotExist());
    }

    @Test
    void getRunByIdReturnsFrontendCompatibleFields() throws Exception {
        RunEntity run = saveRun(
                BacktestStatus.SUCCEEDED,
                Instant.parse("2024-01-01T00:00:00Z"),
                "{\"fastPeriod\":10}",
                "{\"profit\":9.5}",
                null
        );

        mockMvc.perform(get("/api/runs/" + run.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(run.getId()))
                .andExpect(jsonPath("$.strategyId").value(strategyId))
                .andExpect(jsonPath("$.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.strategyName").value("EMA"))
                .andExpect(jsonPath("$.correlationId").isString())
                .andExpect(jsonPath("$.executionDurationMs").value(5000))
                .andExpect(jsonPath("$.metrics.profit").value(9.5))
                .andExpect(jsonPath("$.parameters.fastPeriod").value(10))
                .andExpect(jsonPath("$.config.fastPeriod").value(10))
                .andExpect(jsonPath("$.runId").doesNotExist())
                .andExpect(jsonPath("$.summary").doesNotExist());
    }

    @Test
    void getRunResultReturnsPersistedArtifacts() throws Exception {
        RunEntity run = saveRun(
                BacktestStatus.SUCCEEDED,
                Instant.parse("2024-01-01T00:00:00Z"),
                "{\"fastPeriod\":10}",
                "{\"profit\":9.5}",
                null
        );

        mockMvc.perform(get("/api/runs/" + run.getId() + "/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.runId").value(run.getId()))
                .andExpect(jsonPath("$.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.executionDurationMs").value(5000))
                .andExpect(jsonPath("$.metrics.profit").value(9.5));
    }

    @Test
    void getRunByIdReturnsNotFoundForMissingRun() throws Exception {
        mockMvc.perform(get("/api/runs/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void postRunCreatesAndReturnsFullRunObject() throws Exception {
        when(pythonParserClient.executeRun(any())).thenReturn(pythonRunExecuteResponse());

        mockMvc.perform(post("/api/runs")
                        .contentType("application/json")
                        .content("""
                                {
                                  "strategyId": %d,
                                  "exchange": "binance",
                                  "symbol": "BTCUSDT",
                                  "interval": "1h",
                                  "from": "2024-01-01T00:00:00Z",
                                  "to": "2024-01-01T01:00:00Z",
                                  "params": {
                                    "fastPeriod": 10
                                  },
                                  "initialCash": 10000.0,
                                  "feeRate": 0.001,
                                  "slippageBps": 1.0,
                                  "strictData": true
                                }
                                """.formatted(strategyId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.strategyId").value(strategyId))
                .andExpect(jsonPath("$.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.strategyName").value("EMA"))
                .andExpect(jsonPath("$.correlationId").isString())
                .andExpect(jsonPath("$.executionDurationMs").isNumber())
                .andExpect(jsonPath("$.metrics.profit").value(9.5))
                .andExpect(jsonPath("$.parameters.fastPeriod").value(10))
                .andExpect(jsonPath("$.config.strategyId").value(strategyId))
                .andExpect(jsonPath("$.summary.profit").value(9.5))
                .andExpect(jsonPath("$.artifacts.tradesCount").value(1))
                .andExpect(jsonPath("$.runId").doesNotExist())
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    void postRunPersistsStructuredPythonFailure() throws Exception {
        PythonRunExecuteResponse response = new PythonRunExecuteResponse();
        response.setSuccess(false);
        response.setRunId("1");
        response.setCorrelationId("run-1");
        response.setStartedAt("2024-01-01T00:00:00Z");
        response.setFinishedAt("2024-01-01T00:00:01Z");
        response.setExecutionDurationMs(1000L);
        response.setEngineVersion("python-execution-engine/0.2.1-alpha.1");
        response.setErrorCode("STRATEGY_RUNTIME_ERROR");
        response.setErrorMessage("Strategy.run raised exception: boom");
        response.setStacktrace("ValueError: boom");
        response.setError("Strategy.run raised exception: boom");

        when(pythonParserClient.executeRun(any())).thenReturn(response);

        mockMvc.perform(post("/api/runs")
                        .contentType("application/json")
                        .content("""
                                {
                                  "strategyId": %d,
                                  "exchange": "binance",
                                  "symbol": "BTCUSDT",
                                  "interval": "1h",
                                  "from": "2024-01-01T00:00:00Z",
                                  "to": "2024-01-01T01:00:00Z",
                                  "params": {
                                    "fastPeriod": 10
                                  },
                                  "initialCash": 10000.0,
                                  "feeRate": 0.001,
                                  "slippageBps": 1.0,
                                  "strictData": true
                                }
                                """.formatted(strategyId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.errorMessage").value("Strategy.run raised exception: boom"))
                .andExpect(jsonPath("$.errorDetails.source").value("python"))
                .andExpect(jsonPath("$.errorDetails.errorCode").value("STRATEGY_RUNTIME_ERROR"))
                .andExpect(jsonPath("$.errorDetails.stacktrace").value("ValueError: boom"))
                .andExpect(jsonPath("$.executionDurationMs").isNumber());
    }

    @Test
    void rerunReusesStoredConfiguration() throws Exception {
        RunEntity run = saveRun(
                BacktestStatus.SUCCEEDED,
                Instant.parse("2024-01-01T00:00:00Z"),
                """
                        {
                          "strategyId": %d,
                          "exchange": "binance",
                          "symbol": "BTCUSDT",
                          "interval": "1h",
                          "from": "2024-01-01T00:00:00Z",
                          "to": "2024-01-01T02:00:00Z",
                          "params": {
                            "fastPeriod": 10
                          },
                          "initialCash": 10000.0,
                          "feeRate": 0.001,
                          "slippageBps": 1.0,
                          "strictData": true
                        }
                        """.formatted(strategyId),
                "{\"profit\":9.5}",
                null
        );

        when(pythonParserClient.executeRun(any())).thenReturn(pythonRunExecuteResponse());

        mockMvc.perform(post("/api/runs/" + run.getId() + "/rerun"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.config.strategyId").value(strategyId))
                .andExpect(jsonPath("$.parameters.fastPeriod").value(10))
                .andExpect(jsonPath("$.status").value("SUCCEEDED"));
    }

    private RunEntity saveRun(
            BacktestStatus status,
            Instant createdAt,
            String paramsJson,
            String metricsJson,
            String errorMessage
    ) {
        RunEntity entity = new RunEntity();
        entity.setStrategyId(strategyId);
        entity.setStrategyName("EMA");
        entity.setCorrelationId("run-" + (runRepository.count() + 1));
        entity.setStatus(status);
        entity.setExchange("binance");
        entity.setSymbol("BTCUSDT");
        entity.setInterval("1h");
        entity.setDateFrom(Instant.parse("2024-01-01T00:00:00Z"));
        entity.setDateTo(Instant.parse("2024-01-01T02:00:00Z"));
        entity.setParamsJson(paramsJson);
        entity.setMetricsJson(metricsJson);
        entity.setErrorMessage(errorMessage);
        entity.setErrorDetailsJson(errorMessage == null ? null : "{\"source\":\"python\"}");
        entity.setCreatedAt(createdAt);
        entity.setStartedAt(createdAt.plusSeconds(5));
        entity.setFinishedAt(createdAt.plusSeconds(10));
        entity.setExecutionDurationMs(5000L);
        return runRepository.saveAndFlush(entity);
    }

    private CandleEntity candle(String openTime) {
        Instant open = Instant.parse(openTime);
        return new CandleEntity(
                null,
                "binance",
                "BTCUSDT",
                "1h",
                open,
                open.plusSeconds(3600),
                new BigDecimal("1.0"),
                new BigDecimal("2.0"),
                new BigDecimal("0.5"),
                new BigDecimal("1.5"),
                new BigDecimal("10.0")
        );
    }

    private BacktestResult backtestResult() {
        BacktestTrade trade = new BacktestTrade();
        trade.setEntryTime(Instant.parse("2024-01-01T00:00:00Z"));
        trade.setExitTime(Instant.parse("2024-01-01T01:00:00Z"));
        trade.setEntryPrice(100.0);
        trade.setExitPrice(109.5);
        trade.setQuantity(1.0);
        trade.setPnl(9.5);
        trade.setFee(0.2);

        EquityPoint point = new EquityPoint();
        point.setTimestamp(Instant.parse("2024-01-01T01:00:00Z"));
        point.setEquity(10_009.5);

        BacktestResult result = new BacktestResult();
        result.setSummary(Map.of("profit", 9.5));
        result.setTrades(List.of(trade));
        result.setEquityCurve(List.of(point));
        result.setLogs(List.of("done"));
        result.setWarnings(List.of());
        return result;
    }

    private PythonRunExecuteResponse pythonRunExecuteResponse() {
        BacktestResult result = backtestResult();
        PythonRunExecuteResponse response = new PythonRunExecuteResponse();
        response.setSuccess(true);
        response.setSummary(Map.of("profit", 9.5));
        response.setMetrics(Map.of("profit", 9.5));
        response.setTrades(result.getTrades());
        response.setEquityCurve(result.getEquityCurve());
        response.setArtifacts(Map.of("tradesCount", 1, "equityPointCount", 1));
        response.setEngineVersion("python-execution-engine/0.2.1-alpha.1");
        response.setRunId("1");
        response.setCorrelationId("run-1");
        response.setStartedAt("2024-01-01T00:00:00Z");
        response.setFinishedAt("2024-01-01T00:00:01Z");
        response.setExecutionDurationMs(1000L);
        response.setError(null);
        return response;
    }
}
