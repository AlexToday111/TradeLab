package com.example.back.livetrading.dto;

import java.time.Instant;

public record CircuitBreakerResponse(
        String exchange,
        boolean active,
        String reason,
        Instant triggeredAt,
        Instant updatedAt
) {
}
