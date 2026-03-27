package com.example.back.service;

import com.example.back.imports.client.PythonParserClient;
import com.example.back.dto.CreateRunRequest;
import com.example.back.dto.PythonRunExecuteRequest;
import com.example.back.dto.PythonRunExecuteResponse;
import com.example.back.dto.RunResponse;
import com.example.back.entity.RunEntity;
import com.example.back.entity.StrategyFileEntity;
import com.example.back.repository.RunRepository;
import com.example.back.repository.StrategyFileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RunService {

    private final StrategyFileRepository strategyFileRepository;
    private final RunRepository runRepository;
    private final PythonParserClient pythonParserClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public RunResponse createRun(CreateRunRequest request) {
        StrategyFileEntity strategy = strategyFileRepository.findById(request.getStrategyId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Стратегия с ID " + request.getStrategyId() + " не найдена"
                ));

        if (!"VALID".equals(String.valueOf(strategy.getStatus()))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Стратегия не валидна. Текущий статус: " + strategy.getStatus()
            );
        }

        RunEntity run = new RunEntity();
        run.setStrategyId(strategy.getId());
        run.setStatus(RunEntity.RunStatus.PENDING);
        run.setExchange(request.getExchange());
        run.setSymbol(request.getSymbol());
        run.setInterval(request.getInterval());
        run.setDateFrom(parseInstant(request.getFrom(), "from"));
        run.setDateTo(parseInstant(request.getTo(), "to"));
        run.setParamsJson(writeJson(request.getParams()));
        run.setCreatedAt(Instant.now());

        run = runRepository.save(run);

        try {
            PythonRunExecuteRequest pythonRequest = new PythonRunExecuteRequest();
            pythonRequest.setStrategyFilePath(strategy.getStoragePath());
            pythonRequest.setExchange(request.getExchange());
            pythonRequest.setSymbol(request.getSymbol());
            pythonRequest.setInterval(request.getInterval());
            pythonRequest.setFrom(request.getFrom());
            pythonRequest.setTo(request.getTo());
            pythonRequest.setParams(request.getParams());

            run.setStatus(RunEntity.RunStatus.RUNNING);
            run = runRepository.save(run);

            PythonRunExecuteResponse pythonResponse = pythonParserClient.executeRun(pythonRequest);

            if (pythonResponse != null && pythonResponse.getSuccess()) {
                run.setStatus(RunEntity.RunStatus.COMPLETED);
                run.setMetricsJson(writeJson(pythonResponse.getMetrics()));
                run.setFinishedAt(Instant.now());
            } else {
                run.setStatus(RunEntity.RunStatus.FAILED);
                run.setErrorMessage(
                        pythonResponse != null ? pythonResponse.getError() : "Python сервис вернул пустой ответ"
                );
                run.setFinishedAt(Instant.now());
            }

        } catch (Exception e) {
            log.error("Ошибка при вызове Python сервиса", e);
            run.setStatus(RunEntity.RunStatus.FAILED);
            run.setErrorMessage("Ошибка при выполнении: " + e.getMessage());
            run.setFinishedAt(Instant.now());
        }

        run = runRepository.save(run);
        return mapToResponse(run);
    }

    public List<RunResponse> getRuns() {
        return runRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public RunResponse getRunById(Long id) {
        RunEntity run = runRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Run с ID " + id + " не найден"
                ));
        return mapToResponse(run);
    }

    private RunResponse mapToResponse(RunEntity run) {
        RunResponse response = new RunResponse();
        response.setId(run.getId());
        response.setStrategyId(run.getStrategyId());
        response.setStatus(run.getStatus().name());
        response.setExchange(run.getExchange());
        response.setSymbol(run.getSymbol());
        response.setInterval(run.getInterval());
        response.setFrom(run.getDateFrom() != null ? run.getDateFrom().toString() : null);
        response.setTo(run.getDateTo() != null ? run.getDateTo().toString() : null);
        response.setParams(readJsonMap(run.getParamsJson()));
        response.setMetrics(readJsonMap(run.getMetricsJson()));
        response.setErrorMessage(run.getErrorMessage());
        response.setCreatedAt(run.getCreatedAt());
        response.setFinishedAt(run.getFinishedAt());
        return response;
    }

    private Instant parseInstant(String value, String fieldName) {
        try {
            return Instant.parse(value);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Некорректное поле " + fieldName + ": " + value
            );
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось сериализовать JSON"
            );
        }
    }

    private Map<String, Object> readJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Не удалось прочитать JSON: {}", json, e);
            return Collections.emptyMap();
        }
    }
}
