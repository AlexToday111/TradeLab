package com.example.back.runs.dto;

import java.time.Instant;
import java.util.Map;
import lombok.Builder;

@Builder
public record RunResponse(
        Long id,
        Long strategyId,
        RunStatusResponse status,
        String exchange,
        String symbol,
        String interval,
        Instant from,
        Instant to,
        Instant createdAt,
        Instant startedAt,
        Instant finishedAt,
        Map<String, Object> parameters,
        Map<String, Object> metrics,
        String errorMessage
) {
}
