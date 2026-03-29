package com.example.back.candles.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Свеча рыночных данных")
public record CandleResponse(

        @Schema(description = "Биржа", example = "binance")
        String exchange,

        @Schema(description = "Торговый символ", example = "BTCUSDT")
        String symbol,

        @Schema(description = "Интервал свечи", example = "1h")
        String interval,

        @Schema(description = "Время открытия свечи (UTC)", example = "2024-01-01T00:00:00Z")
        Instant openTime,

        @Schema(description = "Время закрытия свечи (UTC)", example = "2024-01-01T01:00:00Z")
        Instant closeTime,

        @Schema(description = "Цена открытия", example = "42000.50")
        BigDecimal open,

        @Schema(description = "Максимальная цена", example = "42150.00")
        BigDecimal high,

        @Schema(description = "Минимальная цена", example = "41890.10")
        BigDecimal low,

        @Schema(description = "Цена закрытия", example = "42080.25")
        BigDecimal close,

        @Schema(description = "Объём", example = "123.456")
        BigDecimal volume
) {}