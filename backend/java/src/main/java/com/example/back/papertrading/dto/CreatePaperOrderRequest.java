package com.example.back.papertrading.dto;

import com.example.back.papertrading.entity.PaperOrderSide;
import com.example.back.papertrading.entity.PaperOrderType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreatePaperOrderRequest(
        @NotNull PaperOrderSide side,
        @NotNull PaperOrderType type,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantity,
        BigDecimal price
) {
}
