package com.example.back.livetrading.service;

import com.example.back.livetrading.entity.LiveOrderStatus;
import java.math.BigDecimal;

public record LiveOrderResult(
        String exchangeOrderId,
        LiveOrderStatus status,
        BigDecimal executedPrice,
        String rejectedReason
) {
}
