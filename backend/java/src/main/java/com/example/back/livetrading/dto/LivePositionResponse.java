package com.example.back.livetrading.dto;

import com.example.back.livetrading.entity.LivePositionSyncStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record LivePositionResponse(
        Long id,
        Long userId,
        String exchange,
        String symbol,
        BigDecimal quantity,
        BigDecimal averageEntryPrice,
        BigDecimal realizedPnl,
        BigDecimal unrealizedPnl,
        Instant updatedAt,
        LivePositionSyncStatus syncStatus
) {
}
