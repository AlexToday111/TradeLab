package com.example.back.runs.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.back.imports.client.PythonParserClient;
import com.example.back.runs.dto.CreateRunRequest;
import com.example.back.runs.dto.PythonRunExecuteResponse;
import com.example.back.runs.entity.RunEntity;
import com.example.back.runs.repository.RunRepository;
import com.example.back.strategies.entity.StrategyFileEntity;
import com.example.back.strategies.repository.StrategyFileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class RunServiceTest {

    @Mock
    private StrategyFileRepository strategyFileRepository;

    @Mock
    private RunRepository runRepository;

    @Mock
    private PythonParserClient pythonParserClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RunService runService;

    @BeforeEach
    void setUp() {
        runService = new RunService(strategyFileRepository, runRepository, pythonParserClient, objectMapper);
    }

    @Test
    void createRunCompletesSuccessfullyWhenPythonReturnsMetrics() {
        StrategyFileEntity strategy = validStrategy();
        when(strategyFileRepository.findById(1L)).thenReturn(Optional.of(strategy));
        AtomicLong ids = new AtomicLong(100);
        when(runRepository.save(any(RunEntity.class))).thenAnswer(invocation -> {
            RunEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(ids.incrementAndGet());
            }
            return entity;
        });
        when(pythonParserClient.executeRun(any())).thenReturn(
            new PythonRunExecuteResponse(true, Map.of("profit", 42), null)
        );

        var response = runService.createRun(createRunRequest());

        assertThat(response.getStatus()).isEqualTo(RunEntity.RunStatus.COMPLETED.name());
        assertThat(response.getMetrics()).containsEntry("profit", 42);
        verify(runRepository, times(3)).save(any(RunEntity.class));
    }

    @Test
    void createRunMarksRunAsFailedWhenPythonReturnsError() {
        StrategyFileEntity strategy = validStrategy();
        when(strategyFileRepository.findById(1L)).thenReturn(Optional.of(strategy));
        when(runRepository.save(any(RunEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pythonParserClient.executeRun(any())).thenReturn(
            new PythonRunExecuteResponse(false, null, "execution failed")
        );

        var response = runService.createRun(createRunRequest());

        assertThat(response.getStatus()).isEqualTo(RunEntity.RunStatus.FAILED.name());
        assertThat(response.getErrorMessage()).isEqualTo("execution failed");
    }

    @Test
    void createRunRejectsUnknownStrategy() {
        when(strategyFileRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> runService.createRun(createRunRequest()))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createRunRejectsInvalidDate() {
        StrategyFileEntity strategy = validStrategy();
        when(strategyFileRepository.findById(1L)).thenReturn(Optional.of(strategy));

        CreateRunRequest request = createRunRequest();
        request.setFrom("bad-date");

        assertThatThrownBy(() -> runService.createRun(request))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getRunsMapsStoredJsonFields() throws Exception {
        RunEntity run = new RunEntity();
        run.setId(1L);
        run.setStrategyId(1L);
        run.setStatus(RunEntity.RunStatus.COMPLETED);
        run.setExchange("binance");
        run.setSymbol("BTCUSDT");
        run.setInterval("1h");
        run.setDateFrom(Instant.parse("2024-01-01T00:00:00Z"));
        run.setDateTo(Instant.parse("2024-01-02T00:00:00Z"));
        run.setParamsJson(objectMapper.writeValueAsString(Map.of("length", 20)));
        run.setMetricsJson(objectMapper.writeValueAsString(Map.of("profit", 10)));
        run.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        when(runRepository.findAll()).thenReturn(List.of(run));

        var runs = runService.getRuns();

        assertThat(runs).hasSize(1);
        assertThat(runs.get(0).getParams()).containsEntry("length", 20);
        assertThat(runs.get(0).getMetrics()).containsEntry("profit", 10);
    }

    @Test
    void getRunByIdFailsWhenRunDoesNotExist() {
        when(runRepository.findById(55L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> runService.getRunById(55L))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    private StrategyFileEntity validStrategy() {
        StrategyFileEntity strategy = new StrategyFileEntity();
        strategy.setId(1L);
        strategy.setName("EMA");
        strategy.setFileName("ema.py");
        strategy.setStoragePath("C:/strategies/ema.py");
        strategy.setStatus(StrategyFileEntity.StrategyStatus.VALID);
        strategy.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        return strategy;
    }

    private CreateRunRequest createRunRequest() {
        CreateRunRequest request = new CreateRunRequest();
        request.setStrategyId(1L);
        request.setExchange("binance");
        request.setSymbol("BTCUSDT");
        request.setInterval("1h");
        request.setFrom("2024-01-01T00:00:00Z");
        request.setTo("2024-01-02T00:00:00Z");
        request.setParams(Map.of("length", 20));
        return request;
    }
}
