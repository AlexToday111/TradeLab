package com.example.back.livetrading.service;

import java.math.BigDecimal;

public record ExchangePositionSnapshot(
        String symbol,
        BigDecimal quantity,
        BigDecimal averageEntryPrice,
        BigDecimal realizedPnl,
        BigDecimal unrealizedPnl
) {
}
