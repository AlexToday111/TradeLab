package com.example.back.backtest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.back.backtest.dto.BacktestRequest;
import com.example.back.backtest.dto.BacktestResult;
import com.example.back.backtest.dto.BacktestTrade;
import com.example.back.backtest.dto.CreateBacktestRunRequest;
import com.example.back.backtest.dto.EquityPoint;
import com.example.back.backtest.exception.BacktestValidationException;
import com.example.back.backtest.executor.PythonBacktestExecutor;
import com.example.back.backtest.executor.PythonExecutionException;
import com.example.back.backtest.model.BacktestStatus;
import com.example.back.backtest.repository.BacktestEquityPointRepository;
import com.example.back.backtest.repository.BacktestTradeRepository;
import com.example.back.candles.entity.CandleEntity;
import com.example.back.candles.repository.CandleRepository;
import com.example.back.runs.entity.RunEntity;
import com.example.back.runs.repository.RunRepository;
import com.example.back.runs.service.RunFailureStateService;
import com.example.back.runs.service.RunQueryService;
import com.example.back.strategies.entity.StrategyFileEntity;
import com.example.back.strategies.repository.StrategyFileRepository;
import com.example.back.telegram.service.TelegramNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

class BacktestServiceTest {

    private final RunRepository runRepository = mock(RunRepository.class);
    private final StrategyFileRepository strategyFileRepository = mock(StrategyFileRepository.class);
    private final CandleRepository candleRepository = mock(CandleRepository.class);
    private final BacktestTradeRepository backtestTradeRepository = mock(BacktestTradeRepository.class);
    private final BacktestEquityPointRepository backtestEquityPointRepository = mock(BacktestEquityPointRepository.class);
    private final PythonBacktestExecutor pythonBacktestExecutor = mock(PythonBacktestExecutor.class);
    private final RunFailureStateService runFailureStateService = mock(RunFailureStateService.class);
    private final RunQueryService runQueryService = mock(RunQueryService.class);
    private final TelegramNotificationService telegramNotificationService = mock(TelegramNotificationService.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private BacktestService backtestService;

    @BeforeEach
    void setUp() {
        backtestService = new BacktestService(
                runRepository,
                strategyFileRepository,
                candleRepository,
                backtestTradeRepository,
                backtestEquityPointRepository,
                pythonBacktestExecutor,
                runFailureStateService,
                runQueryService,
                telegramNotificationService,
                objectMapper,
                transactionManager()
        );
    }

    @Test
    void createsRunAndStoresSerializedRequest() {
        StrategyFileEntity strategy = validStrategy();
        when(strategyFileRepository.findById(7L)).thenReturn(Optional.of(strategy));
        AtomicLong ids = new AtomicLong(100);
        when(runRepository.save(any(RunEntity.class))).thenAnswer(invocation -> {
            RunEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(ids.incrementAndGet());
            }
            return entity;
        });

        Long runId = backtestService.createRun(createRequest());

        assertThat(runId).isEqualTo(101L);
        ArgumentCaptor<RunEntity> runCaptor = ArgumentCaptor.forClass(RunEntity.class);
        verify(runRepository).save(runCaptor.capture());
        assertThat(runCaptor.getValue().getStatus()).isEqualTo(BacktestStatus.PENDING);
        assertThat(runCaptor.getValue().getParamsJson()).contains("\"strategyId\":7");
        assertThat(runCaptor.getValue().getParamsJson()).contains("\"fastPeriod\":10");
    }

    @Test
    void executesRunAndPersistsArtifacts(@TempDir Path tempDir) throws Exception {
        StrategyFileEntity strategy = validStrategy();
        strategy.setStoragePath(tempDir.resolve("ema.py").toString());
        Files.writeString(Path.of(strategy.getStoragePath()), "class Strategy:\n    pass\n");

        RunEntity run = storedRun();
        when(runRepository.findById(11L)).thenReturn(Optional.of(run));
        when(strategyFileRepository.findById(7L)).thenReturn(Optional.of(strategy));
        when(candleRepository
                .findByExchangeAndSymbolAndIntervalAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualOrderByOpenTimeAsc(
                        eq("binance"),
                        eq("BTCUSDT"),
                        eq("1h"),
                        eq(run.getDateFrom()),
                        eq(run.getDateTo())
                ))
                .thenReturn(List.of(candle("2024-01-01T00:00:00Z"), candle("2024-01-01T01:00:00Z")));

        ArgumentCaptor<BacktestRequest> executorRequest = ArgumentCaptor.forClass(BacktestRequest.class);
        when(pythonBacktestExecutor.execute(executorRequest.capture())).thenAnswer(invocation -> {
            BacktestRequest request = invocation.getArgument(0);
            String csv = Files.readString(Path.of(request.getDataPath()));
            assertThat(csv).contains("timestamp,open,high,low,close,volume");
            assertThat(csv).contains("2024-01-01T00:00:00Z,1,2,0.5,1.5,10");
            return backtestResult();
        });

        var response = backtestService.executeRun(11L);

        assertThat(response.status()).isEqualTo(BacktestStatus.COMPLETED);
        assertThat(response.summary()).containsEntry("profit", 12.5);
        assertThat(executorRequest.getValue().getStrategyPath()).isEqualTo(strategy.getStoragePath());
        assertThat(executorRequest.getValue().getInitialCash()).isEqualTo(10_000.0);
        verify(backtestTradeRepository).saveAll(any());
        verify(backtestEquityPointRepository).saveAll(any());
        assertThat(run.getFinishedAt()).isNotNull();
    }

    @Test
    void marksRunAsFailedWhenPythonFails() {
        StrategyFileEntity strategy = validStrategy();
        RunEntity run = storedRun();
        when(runRepository.findById(11L)).thenReturn(Optional.of(run));
        when(strategyFileRepository.findById(7L)).thenReturn(Optional.of(strategy));
        when(candleRepository
                .findByExchangeAndSymbolAndIntervalAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualOrderByOpenTimeAsc(
                        eq("binance"),
                        eq("BTCUSDT"),
                        eq("1h"),
                        eq(run.getDateFrom()),
                        eq(run.getDateTo())
                ))
                .thenReturn(List.of(candle("2024-01-01T00:00:00Z")));
        when(pythonBacktestExecutor.execute(any(BacktestRequest.class)))
                .thenThrow(new PythonExecutionException("boom"));
        doAnswer(invocation -> {
            run.setStatus(BacktestStatus.FAILED);
            run.setErrorMessage(invocation.getArgument(1));
            run.setFinishedAt(Instant.now());
            return null;
        }).when(runFailureStateService).markFailedInNewTransaction(eq(11L), eq("boom"));

        assertThatThrownBy(() -> backtestService.executeRun(11L))
                .isInstanceOf(PythonExecutionException.class)
                .hasMessageContaining("boom");

        assertThat(run.getStatus()).isEqualTo(BacktestStatus.FAILED);
        assertThat(run.getFinishedAt()).isNotNull();
        assertThat(run.getErrorMessage()).contains("boom");
        verify(runFailureStateService).markFailedInNewTransaction(11L, "boom");
        verify(runRepository, atLeastOnce()).save(run);
    }

    @Test
    void rejectsInvalidTimeRange() {
        CreateBacktestRunRequest request = createRequest();
        request.setFrom(Instant.parse("2024-01-03T00:00:00Z"));
        request.setTo(Instant.parse("2024-01-01T00:00:00Z"));

        assertThatThrownBy(() -> backtestService.createRun(request))
                .isInstanceOf(BacktestValidationException.class)
                .hasMessageContaining("must be before");
    }

    private PlatformTransactionManager transactionManager() {
        PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
        TransactionStatus status = new SimpleTransactionStatus();
        when(manager.getTransaction(any(TransactionDefinition.class))).thenReturn(status);
        return manager;
    }

    private StrategyFileEntity validStrategy() {
        StrategyFileEntity strategy = new StrategyFileEntity();
        strategy.setId(7L);
        strategy.setFileName("ema.py");
        strategy.setName("EMA");
        strategy.setStoragePath("/tmp/ema.py");
        strategy.setStatus(StrategyFileEntity.StrategyStatus.VALID);
        strategy.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        return strategy;
    }

    private RunEntity storedRun() {
        RunEntity run = new RunEntity();
        run.setId(11L);
        run.setStrategyId(7L);
        run.setStatus(BacktestStatus.PENDING);
        run.setExchange("binance");
        run.setSymbol("BTCUSDT");
        run.setInterval("1h");
        run.setDateFrom(Instant.parse("2024-01-01T00:00:00Z"));
        run.setDateTo(Instant.parse("2024-01-01T02:00:00Z"));
        run.setParamsJson("""
                {
                  "strategyId": 7,
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
                  "slippageBps": 2.0,
                  "strictData": true
                }
                """);
        run.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        return run;
    }

    private CreateBacktestRunRequest createRequest() {
        CreateBacktestRunRequest request = new CreateBacktestRunRequest();
        request.setStrategyId(7L);
        request.setExchange("binance");
        request.setSymbol("BTCUSDT");
        request.setInterval("1h");
        request.setFrom(Instant.parse("2024-01-01T00:00:00Z"));
        request.setTo(Instant.parse("2024-01-01T02:00:00Z"));
        request.setParams(Map.of("fastPeriod", 10));
        request.setInitialCash(10_000.0);
        request.setFeeRate(0.001);
        request.setSlippageBps(2.0);
        request.setStrictData(Boolean.TRUE);
        return request;
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
        trade.setExitPrice(105.0);
        trade.setQuantity(1.0);
        trade.setPnl(5.0);
        trade.setFee(0.2);

        EquityPoint point = new EquityPoint();
        point.setTimestamp(Instant.parse("2024-01-01T00:00:00Z"));
        point.setEquity(10_005.0);

        BacktestResult result = new BacktestResult();
        result.setSummary(Map.of("profit", 12.5));
        result.setTrades(List.of(trade));
        result.setEquityCurve(List.of(point));
        result.setLogs(List.of("done"));
        result.setWarnings(List.of());
        return result;
    }
}
