package com.example.back.livetrading.dto;

import com.example.back.livetrading.entity.LiveSessionStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record LiveSessionResponse(
        Long id,
        Long userId,
        String name,
        String exchange,
        String symbol,
        String baseCurrency,
        String quoteCurrency,
        LiveSessionStatus status,
        BigDecimal maxOrderNotional,
        BigDecimal maxPositionNotional,
        BigDecimal maxDailyNotional,
        String symbolWhitelist,
        Instant createdAt,
        Instant updatedAt
) {
}
