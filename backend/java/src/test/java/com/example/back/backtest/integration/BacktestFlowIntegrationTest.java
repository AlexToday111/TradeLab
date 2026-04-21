package com.example.back.backtest.integration;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.example.back.backtest.repository.BacktestEquityPointRepository;
import com.example.back.backtest.repository.BacktestTradeRepository;
import com.example.back.candles.entity.CandleEntity;
import com.example.back.candles.repository.CandleRepository;
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
        "spring.datasource.url=jdbc:h2:mem:backtest-flow;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=INTERVAL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
class BacktestFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StrategyFileRepository strategyFileRepository;

    @Autowired
    private RunRepository runRepository;

    @Autowired
    private BacktestTradeRepository backtestTradeRepository;

    @Autowired
    private BacktestEquityPointRepository backtestEquityPointRepository;

    @MockBean
    private PythonBacktestExecutor pythonBacktestExecutor;

    @MockBean
    private CandleRepository candleRepository;

    private Long strategyId;

    @BeforeEach
    void setUp() {
        backtestEquityPointRepository.deleteAll();
        backtestTradeRepository.deleteAll();
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
    void fullBacktestFlowPersistsAndReturnsArtifacts() throws Exception {
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
                        candle("2024-01-01T01:00:00Z"),
                        candle("2024-01-01T02:00:00Z")
                ));
        when(pythonBacktestExecutor.execute(any())).thenReturn(backtestResult());

        String responseBody = mockMvc.perform(post("/backtests")
                        .contentType("application/json")
                        .content("""
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
                                """.formatted(strategyId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.runId").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long runId = Long.parseLong(responseBody.replaceAll("\\D+", ""));

        mockMvc.perform(get("/backtests/" + runId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.summary.profit").value(9.5))
                .andExpect(jsonPath("$.params.fastPeriod").value(10));

        mockMvc.perform(get("/backtests/" + runId + "/trades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pnl").value(9.5));

        mockMvc.perform(get("/backtests/" + runId + "/equity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].equity").value(10009.5));

        assertThat(runRepository.findById(runId)).isPresent();
        assertThat(runRepository.findById(runId).orElseThrow().getStatus()).isEqualTo(BacktestStatus.SUCCEEDED);
        assertThat(backtestTradeRepository.findByRunIdOrderByEntryTimeAsc(runId)).hasSize(1);
        assertThat(backtestEquityPointRepository.findByRunIdOrderByTimestampAsc(runId)).hasSize(1);
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
