package com.example.back.livetrading.dto;

import com.example.back.livetrading.entity.LiveOrderSide;
import com.example.back.livetrading.entity.LiveOrderStatus;
import com.example.back.livetrading.entity.LiveOrderType;
import java.math.BigDecimal;
import java.time.Instant;

public record LiveOrderResponse(
        Long id,
        Long userId,
        Long sessionId,
        Long strategyId,
        Long strategyVersionId,
        String exchange,
        String symbol,
        LiveOrderSide side,
        LiveOrderType type,
        BigDecimal quantity,
        BigDecimal requestedPrice,
        BigDecimal executedPrice,
        LiveOrderStatus status,
        String exchangeOrderId,
        Instant submittedAt,
        Instant updatedAt,
        Instant filledAt,
        String rejectedReason,
        Long sourceRunId
) {
}
