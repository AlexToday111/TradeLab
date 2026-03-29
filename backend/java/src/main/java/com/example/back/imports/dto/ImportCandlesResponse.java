package com.example.back.imports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Результат импорта свечей")
public class ImportCandlesResponse {

    @Schema(
            description = "Статус операции",
            example = "success"
    )
    private String status;

    @Schema(
            description = "Биржа",
            example = "binance"
    )
    private String exchange;

    @Schema(
            description = "Торговая пара",
            example = "BTCUSDT"
    )
    private String symbol;

    @Schema(
            description = "Интервал свечей",
            example = "1h"
    )
    private String interval;

    @Schema(
            description = "Количество импортированных свечей",
            example = "744"
    )
    private int imported;

    @Schema(
            description = "Начало диапазона (ISO-8601)",
            example = "2024-01-01T00:00:00Z"
    )
    private String from;

    @Schema(
            description = "Конец диапазона (ISO-8601)",
            example = "2024-01-31T23:59:59Z"
    )
    private String to;
}