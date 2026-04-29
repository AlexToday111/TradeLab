package com.example.back.livetrading.dto;

import java.time.Instant;
import java.util.List;

public record LiveRiskStatusResponse(
        boolean killSwitchActive,
        String killSwitchReason,
        Instant killSwitchActivatedAt,
        List<CircuitBreakerResponse> circuitBreakers
) {
}
