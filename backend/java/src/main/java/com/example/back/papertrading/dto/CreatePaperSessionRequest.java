package com.example.back.papertrading.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreatePaperSessionRequest(
        @NotBlank String name,
        @NotBlank String exchange,
        @NotBlank String symbol,
        @NotBlank String timeframe,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal initialBalance,
        @NotBlank String baseCurrency,
        @NotBlank String quoteCurrency
) {
}
