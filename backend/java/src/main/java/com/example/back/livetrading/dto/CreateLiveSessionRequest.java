package com.example.back.livetrading.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateLiveSessionRequest(
        @NotBlank String name,
        @NotBlank String exchange,
        @NotBlank String symbol,
        @NotBlank String baseCurrency,
        @NotBlank String quoteCurrency,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal maxOrderNotional,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal maxPositionNotional,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal maxDailyNotional,
        String symbolWhitelist
) {
}
