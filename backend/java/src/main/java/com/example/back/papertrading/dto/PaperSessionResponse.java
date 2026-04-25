package com.example.back.papertrading.dto;

import com.example.back.papertrading.entity.PaperSessionStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record PaperSessionResponse(
        Long id,
        Long userId,
        String name,
        String exchange,
        String symbol,
        String timeframe,
        PaperSessionStatus status,
        BigDecimal initialBalance,
        BigDecimal currentBalance,
        String baseCurrency,
        String quoteCurrency,
        Instant startedAt,
        Instant stoppedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
