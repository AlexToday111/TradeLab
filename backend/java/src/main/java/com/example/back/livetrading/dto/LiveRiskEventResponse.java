package com.example.back.livetrading.dto;

import com.example.back.livetrading.entity.LiveRiskEventType;
import java.time.Instant;

public record LiveRiskEventResponse(
        Long id,
        Long orderId,
        Long strategyId,
        String exchange,
        String symbol,
        LiveRiskEventType eventType,
        String reason,
        Instant createdAt
) {
}
