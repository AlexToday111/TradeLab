package com.example.back.imports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Запрос на импорт свечей")
public class ImportCandlesRequest {

    @NotBlank(message = "Биржа не может быть пустой")
    @Schema(
            description = "Название биржи",
            example = "binance",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String exchange;

    @NotBlank(message = "Символ (торговая пара) не может быть пустым")
    @Schema(
            description = "Торговая пара",
            example = "BTCUSDT",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String symbol;

    @NotBlank(message = "Интервал свечей не может быть пустым")
    @Schema(
            description = "Интервал свечей",
            example = "1h",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String interval;

    @NotBlank(message = "Дата начала (from) не может быть пустой")
    @Schema(
            description = "Начало диапазона (ISO-8601)",
            example = "2024-01-01T00:00:00Z",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String from;

    @NotBlank(message = "Дата окончания (to) не может быть пустой")
    @Schema(
            description = "Конец диапазона (ISO-8601)",
            example = "2024-01-31T23:59:59Z",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String to;

    @Schema(
            description = "Bybit category (spot/linear/inverse) when applicable",
            example = "spot"
    )
    private String category;

    @Schema(
            description = "MOEX engine when applicable",
            example = "stock"
    )
    private String engine;

    @Schema(
            description = "MOEX market when applicable",
            example = "shares"
    )
    private String market;

    @Schema(
            description = "MOEX board when applicable",
            example = "TQBR"
    )
    private String board;
}
