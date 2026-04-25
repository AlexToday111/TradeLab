package com.example.back.papertrading.dto;

import com.example.back.papertrading.entity.PaperOrderSide;
import com.example.back.papertrading.entity.PaperOrderStatus;
import com.example.back.papertrading.entity.PaperOrderType;
import java.math.BigDecimal;
import java.time.Instant;

public record PaperOrderResponse(
        Long id,
        Long sessionId,
        Long userId,
        String symbol,
        PaperOrderSide side,
        PaperOrderType type,
        PaperOrderStatus status,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal filledQuantity,
        BigDecimal averageFillPrice,
        Instant createdAt,
        Instant updatedAt,
        Instant filledAt,
        String rejectedReason
) {
}
