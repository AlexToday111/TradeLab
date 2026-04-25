package com.example.back.papertrading.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.back.backtest.executor.PythonBacktestExecutor;
import com.example.back.candles.entity.CandleEntity;
import com.example.back.candles.repository.CandleRepository;
import com.example.back.imports.client.PythonParserClient;
import com.example.back.papertrading.entity.PaperSessionStatus;
import com.example.back.papertrading.entity.PaperTradingSessionEntity;
import com.example.back.papertrading.repository.PaperFillRepository;
import com.example.back.papertrading.repository.PaperOrderRepository;
import com.example.back.papertrading.repository.PaperPositionRepository;
import com.example.back.papertrading.repository.PaperTradingSessionRepository;
import com.example.back.support.TestAuth;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
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
        "spring.datasource.url=jdbc:h2:mem:paper-trading;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=INTERVAL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "execution.jobs.worker-enabled=false"
})
class PaperTradingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaperTradingSessionRepository sessionRepository;

    @Autowired
    private PaperOrderRepository orderRepository;

    @Autowired
    private PaperFillRepository fillRepository;

    @Autowired
    private PaperPositionRepository positionRepository;

    @MockBean
    private CandleRepository candleRepository;

    @MockBean
    private PythonBacktestExecutor pythonBacktestExecutor;

    @MockBean
    private PythonParserClient pythonParserClient;

    @BeforeEach
    void setUp() {
        fillRepository.deleteAll();
        orderRepository.deleteAll();
        positionRepository.deleteAll();
        sessionRepository.deleteAll();
        when(candleRepository.findFirstByExchangeAndSymbolAndIntervalOrderByCloseTimeDesc(
                "binance",
                "BTCUSDT",
                "1h"
        )).thenReturn(Optional.of(latestCandle()));
    }

    @Test
    void createAndMoveSessionThroughLifecycle() throws Exception {
        Long sessionId = createSession();

        mockMvc.perform(post("/api/paper/sessions/" + sessionId + "/start").with(TestAuth.authenticatedRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.startedAt").exists());

        mockMvc.perform(post("/api/paper/sessions/" + sessionId + "/pause").with(TestAuth.authenticatedRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAUSED"));

        mockMvc.perform(post("/api/paper/sessions/" + sessionId + "/start").with(TestAuth.authenticatedRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RUNNING"));

        mockMvc.perform(post("/api/paper/sessions/" + sessionId + "/stop").with(TestAuth.authenticatedRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("STOPPED"))
                .andExpect(jsonPath("$.stoppedAt").exists());
    }

    @Test
    void marketOrderFillsAndUpdatesBalancePositionAndFillHistory() throws Exception {
        Long sessionId = createRunningSession();

        mockMvc.perform(post("/api/paper/sessions/" + sessionId + "/orders")
                        .with(TestAuth.authenticatedRequest())
                        .contentType("application/json")
                        .content("""
                                {
                                  "side": "BUY",
                                  "type": "MARKET",
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("FILLED"))
                .andExpect(jsonPath("$.filledQuantity").value(2.00000000))
                .andExpect(jsonPath("$.averageFillPrice").value(100.00000000));

        mockMvc.perform(get("/api/paper/sessions/" + sessionId).with(TestAuth.authenticatedRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentBalance").value(799.80000000));

        mockMvc.perform(get("/api/paper/sessions/" + sessionId + "/positions").with(TestAuth.authenticatedRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantity").value(2.00000000))
                .andExpect(jsonPath("$[0].averageEntryPrice").value(100.00000000));

        mockMvc.perform(get("/api/paper/sessions/" + sessionId + "/fills").with(TestAuth.authenticatedRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantity").value(2.00000000))
                .andExpect(jsonPath("$[0].fee").value(0.20000000));
    }

    @Test
    void rejectsOrderWithInsufficientBalance() throws Exception {
        Long sessionId = createRunningSession();

        mockMvc.perform(post("/api/paper/sessions/" + sessionId + "/orders")
                        .with(TestAuth.authenticatedRequest())
                        .contentType("application/json")
                        .content("""
                                {
                                  "side": "BUY",
                                  "type": "MARKET",
                                  "quantity": 20
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectedReason").value("Insufficient balance"));

        mockMvc.perform(get("/api/paper/sessions/" + sessionId + "/fills").with(TestAuth.authenticatedRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void rejectsInvalidQuantityWithValidationError() throws Exception {
        Long sessionId = createRunningSession();

        mockMvc.perform(post("/api/paper/sessions/" + sessionId + "/orders")
                        .with(TestAuth.authenticatedRequest())
                        .contentType("application/json")
                        .content("""
                                {
                                  "side": "BUY",
                                  "type": "MARKET",
                                  "quantity": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void enforcesSessionOwnership() throws Exception {
        PaperTradingSessionEntity session = new PaperTradingSessionEntity();
        session.setUserId(999L);
        session.setName("Other user");
        session.setExchange("binance");
        session.setSymbol("BTCUSDT");
        session.setTimeframe("1h");
        session.setStatus(PaperSessionStatus.CREATED);
        session.setInitialBalance(new BigDecimal("1000.00000000"));
        session.setCurrentBalance(new BigDecimal("1000.00000000"));
        session.setBaseCurrency("BTC");
        session.setQuoteCurrency("USDT");
        PaperTradingSessionEntity saved = sessionRepository.saveAndFlush(session);

        mockMvc.perform(get("/api/paper/sessions/" + saved.getId()).with(TestAuth.authenticatedRequest()))
                .andExpect(status().isNotFound());
    }

    @Test
    void acceptedLimitOrderCanBeCanceled() throws Exception {
        Long sessionId = createRunningSession();

        String response = mockMvc.perform(post("/api/paper/sessions/" + sessionId + "/orders")
                        .with(TestAuth.authenticatedRequest())
                        .contentType("application/json")
                        .content("""
                                {
                                  "side": "BUY",
                                  "type": "LIMIT",
                                  "quantity": 1,
                                  "price": 90
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long orderId = Long.valueOf(response.replaceAll(".*\"id\":(\\d+).*", "$1"));

        mockMvc.perform(post("/api/paper/orders/" + orderId + "/cancel").with(TestAuth.authenticatedRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    private Long createSession() throws Exception {
        String response = mockMvc.perform(post("/api/paper/sessions")
                        .with(TestAuth.authenticatedRequest())
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "BTC paper",
                                  "exchange": "binance",
                                  "symbol": "BTCUSDT",
                                  "timeframe": "1h",
                                  "initialBalance": 1000,
                                  "baseCurrency": "BTC",
                                  "quoteCurrency": "USDT"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.userId").value(TestAuth.USER_ID))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return Long.valueOf(response.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    private Long createRunningSession() throws Exception {
        Long sessionId = createSession();
        mockMvc.perform(post("/api/paper/sessions/" + sessionId + "/start").with(TestAuth.authenticatedRequest()))
                .andExpect(status().isOk());
        return sessionId;
    }

    private CandleEntity latestCandle() {
        CandleEntity candle = new CandleEntity();
        candle.setExchange("binance");
        candle.setSymbol("BTCUSDT");
        candle.setInterval("1h");
        candle.setOpenTime(Instant.parse("2024-01-01T00:00:00Z"));
        candle.setCloseTime(Instant.parse("2024-01-01T01:00:00Z"));
        candle.setOpen(new BigDecimal("99.00000000"));
        candle.setHigh(new BigDecimal("101.00000000"));
        candle.setLow(new BigDecimal("98.00000000"));
        candle.setClose(new BigDecimal("100.00000000"));
        candle.setVolume(new BigDecimal("10.00000000"));
        return candle;
    }
}
