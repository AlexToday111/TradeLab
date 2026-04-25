package com.example.back.papertrading.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaperPositionResponse(
        Long id,
        Long sessionId,
        String symbol,
        BigDecimal quantity,
        BigDecimal averageEntryPrice,
        BigDecimal realizedPnl,
        BigDecimal unrealizedPnl,
        Instant updatedAt
) {
}
