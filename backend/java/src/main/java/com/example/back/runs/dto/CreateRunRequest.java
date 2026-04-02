package com.example.back.runs.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@Schema(description = "Запрос на создание запуска стратегии")
public class CreateRunRequest {

    @NotNull(message = "ID стратегии обязателен")
    @Schema(description = "ID стратегии", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long strategyId;

    @NotBlank(message = "Биржа не может быть пустой")
    @Schema(description = "Биржа", example = "binance", requiredMode = Schema.RequiredMode.REQUIRED)
    private String exchange;

    @NotBlank(message = "Символ (торговая пара) не может быть пустым")
    @Schema(description = "Торговая пара", example = "BTCUSDT", requiredMode = Schema.RequiredMode.REQUIRED)
    private String symbol;

    @NotBlank(message = "Интервал свечей не может быть пустым")
    @Schema(description = "Интервал", example = "1h", requiredMode = Schema.RequiredMode.REQUIRED)
    private String interval;

    @NotBlank(message = "Дата начала (from) не может быть пустой")
    @Schema(description = "Начало диапазона", example = "2024-01-01T00:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
    private String from;

    @NotBlank(message = "Дата окончания (to) не может быть пустой")
    @Schema(description = "Конец диапазона", example = "2024-01-31T23:59:59Z", requiredMode = Schema.RequiredMode.REQUIRED)
    private String to;

    @NotNull(message = "Параметры стратегии обязательны")
    @Schema(
            description = "Параметры стратегии",
            example = "{\"period\": 14, \"threshold\": 0.5}",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Map<String, Object> params;
}