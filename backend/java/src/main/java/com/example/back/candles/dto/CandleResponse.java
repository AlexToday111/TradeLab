package com.example.back.candles.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CandleResponse(
    String exchange,
    String symbol,
    String interval,
    Instant openTime,
    Instant closeTime,
    BigDecimal open,
    BigDecimal high,
    BigDecimal low,
    BigDecimal close,
    BigDecimal volume
) {
}
