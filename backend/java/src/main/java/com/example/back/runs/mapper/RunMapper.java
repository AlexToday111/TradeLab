package com.example.back.runs.mapper;

import com.example.back.backtest.model.BacktestStatus;
import com.example.back.runs.dto.RunResponse;
import com.example.back.runs.dto.RunStatusResponse;
import com.example.back.runs.entity.RunEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RunMapper {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public RunResponse toResponse(RunEntity entity) {
        return RunResponse.builder()
                .id(entity.getId())
                .strategyId(entity.getStrategyId())
                .status(toExternalStatus(entity.getStatus()))
                .exchange(entity.getExchange())
                .symbol(entity.getSymbol())
                .interval(entity.getInterval())
                .from(entity.getDateFrom())
                .to(entity.getDateTo())
                .createdAt(entity.getCreatedAt())
                .startedAt(entity.getStartedAt())
                .finishedAt(entity.getFinishedAt())
                .parameters(readParameters(entity.getParamsJson()))
                .metrics(readNullableJsonMap(entity.getMetricsJson()))
                .errorMessage(entity.getErrorMessage())
                .build();
    }

    private RunStatusResponse toExternalStatus(BacktestStatus status) {
        if (status == null) {
            return RunStatusResponse.FAILED;
        }

        return switch (status) {
            case PENDING -> RunStatusResponse.PENDING;
            case RUNNING -> RunStatusResponse.RUNNING;
            case COMPLETED -> RunStatusResponse.SUCCESS;
            case FAILED -> RunStatusResponse.FAILED;
        };
    }

    private Map<String, Object> readJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception ex) {
            log.warn("Failed to parse run JSON payload: {}", json, ex);
            return Collections.emptyMap();
        }
    }

    private Map<String, Object> readNullableJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        return readJsonMap(json);
    }

    private Map<String, Object> readParameters(String json) {
        Map<String, Object> payload = readJsonMap(json);
        Object nestedParams = payload.get("params");
        if (nestedParams instanceof Map<?, ?> params) {
            @SuppressWarnings("unchecked")
            Map<String, Object> typedParams = (Map<String, Object>) params;
            return typedParams;
        }
        return payload;
    }
}
