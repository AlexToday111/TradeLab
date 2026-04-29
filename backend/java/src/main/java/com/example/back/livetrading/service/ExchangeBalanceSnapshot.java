package com.example.back.livetrading.service;

import java.math.BigDecimal;

public record ExchangeBalanceSnapshot(
        String asset,
        BigDecimal free,
        BigDecimal locked
) {
}
