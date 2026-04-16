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

    @MockBean
    private PythonBacktestExecutor pythonBacktestExecutor;

    @MockBean
    private CandleRepository candleRepository;

    private Long strategyId;

    @BeforeEach
    void setUp() {
        runRepository.deleteAll();
        strategyFileRepository.deleteAll();

        StrategyFileEntity strategy = new StrategyFileEntity();
        strategy.setName("EMA");
        strategy.setFileName("ema.py");
        strategy.setStoragePath("/tmp/ema.py");
        strategy.setStatus(StrategyFileEntity.StrategyStatus.VALID);
        strategyId = strategyFileRepository.saveAndFlush(strategy).getId();
    }

    @Test
    void getRunsReturnsSortedListWithFrontendContract() throws Exception {
        RunEntity completedRun = saveRun(
                BacktestStatus.COMPLETED,
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
                .andExpect(jsonPath("$[1].id").value(completedRun.getId()))
                .andExpect(jsonPath("$[1].status").value("SUCCESS"))
                .andExpect(jsonPath("$[1].metrics.profit").value(9.5))
                .andExpect(jsonPath("$[1].parameters.fastPeriod").value(10))
                .andExpect(jsonPath("$[1].config.fastPeriod").value(10))
                .andExpect(jsonPath("$[1].runId").doesNotExist())
                .andExpect(jsonPath("$[1].summary").doesNotExist());
    }

    @Test
    void getRunByIdReturnsFrontendCompatibleFields() throws Exception {
        RunEntity run = saveRun(
                BacktestStatus.COMPLETED,
                Instant.parse("2024-01-01T00:00:00Z"),
                "{\"fastPeriod\":10}",
                "{\"profit\":9.5}",
                null
        );

        mockMvc.perform(get("/api/runs/" + run.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(run.getId()))
                .andExpect(jsonPath("$.strategyId").value(strategyId))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.strategyName").value("EMA"))
                .andExpect(jsonPath("$.correlationId").isString())
                .andExpect(jsonPath("$.metrics.profit").value(9.5))
                .andExpect(jsonPath("$.parameters.fastPeriod").value(10))
                .andExpect(jsonPath("$.config.fastPeriod").value(10))
                .andExpect(jsonPath("$.runId").doesNotExist())
                .andExpect(jsonPath("$.summary").doesNotExist());
    }

    @Test
    void getRunByIdReturnsNotFoundForMissingRun() throws Exception {
        mockMvc.perform(get("/api/runs/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void postRunCreatesAndReturnsFullRunObject() throws Exception {
        when(candleRepository
                .findByExchangeAndSymbolAndIntervalAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualOrderByOpenTimeAsc(
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                ))
                .thenReturn(List.of(
                        candle("2024-01-01T00:00:00Z"),
                        candle("2024-01-01T01:00:00Z")
                ));
        when(pythonBacktestExecutor.execute(any())).thenReturn(backtestResult());

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
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.strategyName").value("EMA"))
                .andExpect(jsonPath("$.correlationId").isString())
                .andExpect(jsonPath("$.metrics.profit").value(9.5))
                .andExpect(jsonPath("$.parameters.fastPeriod").value(10))
                .andExpect(jsonPath("$.config.strategyId").value(strategyId))
                .andExpect(jsonPath("$.artifacts.tradesCount").value(1))
                .andExpect(jsonPath("$.runId").doesNotExist())
                .andExpect(jsonPath("$.summary").doesNotExist());
    }

    @Test
    void rerunReusesStoredConfiguration() throws Exception {
        RunEntity run = saveRun(
                BacktestStatus.COMPLETED,
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

        when(candleRepository
                .findByExchangeAndSymbolAndIntervalAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualOrderByOpenTimeAsc(
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                ))
                .thenReturn(List.of(
                        candle("2024-01-01T00:00:00Z"),
                        candle("2024-01-01T01:00:00Z")
                ));
        when(pythonBacktestExecutor.execute(any())).thenReturn(backtestResult());

        mockMvc.perform(post("/api/runs/" + run.getId() + "/rerun"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.config.strategyId").value(strategyId))
                .andExpect(jsonPath("$.parameters.fastPeriod").value(10))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
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
        entity.setCreatedAt(createdAt);
        entity.setStartedAt(createdAt.plusSeconds(5));
        entity.setFinishedAt(createdAt.plusSeconds(10));
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
}
