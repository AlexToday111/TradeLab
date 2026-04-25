package com.example.back.papertrading.dto;

import com.example.back.papertrading.entity.PaperOrderSide;
import java.math.BigDecimal;
import java.time.Instant;

public record PaperFillResponse(
        Long id,
        Long orderId,
        Long sessionId,
        String symbol,
        PaperOrderSide side,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal fee,
        String feeCurrency,
        Instant executedAt
) {
}
