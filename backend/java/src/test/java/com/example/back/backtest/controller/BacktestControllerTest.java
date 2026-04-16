package com.example.back.backtest.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.back.backtest.dto.BacktestRunResponse;
import com.example.back.backtest.dto.BacktestTrade;
import com.example.back.backtest.dto.EquityPoint;
import com.example.back.backtest.exception.BacktestResourceNotFoundException;
import com.example.back.backtest.model.BacktestStatus;
import com.example.back.backtest.service.BacktestService;
import com.example.back.common.api.GlobalExceptionHandler;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class BacktestControllerTest {

    @Mock
    private BacktestService backtestService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new BacktestController(backtestService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(
                        Jackson2ObjectMapperBuilder.json().findModulesViaServiceLoader(true).build()
                ))
                .build();
    }

    @Test
    void createBacktestReturnsRunId() throws Exception {
        when(backtestService.createRun(any())).thenReturn(101L);
        when(backtestService.executeRun(101L)).thenReturn(runResponse());

        mockMvc.perform(post("/backtests")
                        .contentType("application/json")
                        .content("""
                                {
                                  "strategyId": 42,
                                  "exchange": "binance",
                                  "symbol": "BTCUSDT",
                                  "interval": "1h",
                                  "from": "2024-01-01T00:00:00Z",
                                  "to": "2024-01-03T00:00:00Z",
                                  "params": {
                                    "fastPeriod": 10
                                  },
                                  "initialCash": 10000.0,
                                  "feeRate": 0.001,
                                  "slippageBps": 5.0,
                                  "strictData": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.runId", is(101)));
    }

    @Test
    void getRunReturnsPayload() throws Exception {
        when(backtestService.getRun(101L)).thenReturn(runResponse());

        mockMvc.perform(get("/backtests/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.summary.profit", is(12.5)));
    }

    @Test
    void getTradesReturnsPayload() throws Exception {
        BacktestTrade trade = new BacktestTrade();
        trade.setEntryTime(Instant.parse("2024-01-01T00:00:00Z"));
        trade.setExitTime(Instant.parse("2024-01-01T01:00:00Z"));
        trade.setEntryPrice(100.0);
        trade.setExitPrice(101.0);
        trade.setQuantity(1.0);
        trade.setPnl(1.0);
        trade.setFee(0.1);
        when(backtestService.getTrades(101L)).thenReturn(List.of(trade));

        mockMvc.perform(get("/backtests/101/trades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].entry_price", is(100.0)));
    }

    @Test
    void getEquityReturnsPayload() throws Exception {
        EquityPoint point = new EquityPoint();
        point.setTimestamp(Instant.parse("2024-01-01T00:00:00Z"));
        point.setEquity(10_100.0);
        when(backtestService.getEquity(101L)).thenReturn(List.of(point));

        mockMvc.perform(get("/backtests/101/equity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].equity", is(10100.0)));
    }

    @Test
    void validationErrorsReturnJsonResponse() throws Exception {
        mockMvc.perform(post("/backtests")
                        .contentType("application/json")
                        .content("""
                                {
                                  "strategyId": null,
                                  "exchange": "",
                                  "symbol": "BTCUSDT",
                                  "interval": "1h",
                                  "from": "2024-01-01T00:00:00Z",
                                  "to": "2024-01-03T00:00:00Z",
                                  "params": {}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void notFoundErrorsReturnJsonResponse() throws Exception {
        when(backtestService.getRun(404L)).thenThrow(new BacktestResourceNotFoundException("Run not found: 404"));

        mockMvc.perform(get("/backtests/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Run not found: 404")));
    }

    private BacktestRunResponse runResponse() {
        return new BacktestRunResponse(
                101L,
                42L,
                "EMA",
                "dataset-1",
                "run-101",
                BacktestStatus.COMPLETED,
                "binance",
                "BTCUSDT",
                "1h",
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-03T00:00:00Z"),
                Map.of("fastPeriod", 10),
                Map.of("strategyId", 42, "params", Map.of("fastPeriod", 10)),
                Map.of("profit", 12.5),
                Map.of("tradesCount", 1),
                null,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:01Z"),
                Instant.parse("2024-01-01T00:00:02Z")
        );
    }
}
