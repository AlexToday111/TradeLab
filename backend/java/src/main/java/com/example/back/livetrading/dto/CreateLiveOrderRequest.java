package com.example.back.livetrading.dto;

import com.example.back.livetrading.entity.LiveOrderSide;
import com.example.back.livetrading.entity.LiveOrderType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateLiveOrderRequest(
        @NotNull Long sessionId,
        Long strategyId,
        Long strategyVersionId,
        @NotNull LiveOrderSide side,
        @NotNull LiveOrderType type,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantity,
        BigDecimal requestedPrice,
        Long sourceRunId
) {
}
