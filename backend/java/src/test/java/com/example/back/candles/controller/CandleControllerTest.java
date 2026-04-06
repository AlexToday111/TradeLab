package com.example.back.candles.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.back.candles.dto.CandleResponse;
import com.example.back.candles.service.CandleQueryService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CandleControllerTest {

    @Mock
    private CandleQueryService candleQueryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CandleController(candleQueryService)).build();
    }

    @Test
    void getCandlesReturnsResponseBody() throws Exception {
        when(
            candleQueryService.getCandles(
                "binance",
                "BTCUSDT",
                "1h",
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T01:00:00Z")
            )
        ).thenReturn(
            List.of(
                new CandleResponse(
                    "binance",
                    "BTCUSDT",
                    "1h",
                    Instant.parse("2024-01-01T00:00:00Z"),
                    Instant.parse("2024-01-01T01:00:00Z"),
                    new BigDecimal("1.0"),
                    new BigDecimal("2.0"),
                    new BigDecimal("0.5"),
                    new BigDecimal("1.5"),
                    new BigDecimal("10.0")
                )
            )
        );

        mockMvc.perform(
                get("/api/candles")
                    .param("exchange", "binance")
                    .param("symbol", "BTCUSDT")
                    .param("interval", "1h")
                    .param("from", "2024-01-01T00:00:00Z")
                    .param("to", "2024-01-01T01:00:00Z")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].symbol", is("BTCUSDT")));
    }

    @Test
    void getCandlesRejectsInvalidRange() throws Exception {
        mockMvc.perform(
                get("/api/candles")
                    .param("exchange", "binance")
                    .param("symbol", "BTCUSDT")
                    .param("interval", "1h")
                    .param("from", "2024-01-02T00:00:00Z")
                    .param("to", "2024-01-01T00:00:00Z")
            )
            .andExpect(status().isBadRequest());
    }
}
