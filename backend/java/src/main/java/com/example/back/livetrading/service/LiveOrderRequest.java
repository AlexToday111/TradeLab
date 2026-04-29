package com.example.back.livetrading.service;

import com.example.back.livetrading.entity.LiveOrderSide;
import com.example.back.livetrading.entity.LiveOrderType;
import java.math.BigDecimal;

public record LiveOrderRequest(
        String exchange,
        String symbol,
        LiveOrderSide side,
        LiveOrderType type,
        BigDecimal quantity,
        BigDecimal requestedPrice
) {
}
