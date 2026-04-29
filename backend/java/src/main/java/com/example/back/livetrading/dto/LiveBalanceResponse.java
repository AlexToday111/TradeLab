package com.example.back.livetrading.dto;

import java.math.BigDecimal;

public record LiveBalanceResponse(
        String asset,
        BigDecimal free,
        BigDecimal locked
) {
}
