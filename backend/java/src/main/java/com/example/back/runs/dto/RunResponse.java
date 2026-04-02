package com.example.back.runs.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о запуске стратегии")
public class RunResponse {

    @Schema(description = "ID запуска", example = "1")
    private Long id;

    @Schema(description = "ID стратегии", example = "42")
    private Long strategyId;

    @Schema(description = "Статус выполнения", example = "COMPLETED")
    private String status;

    @Schema(description = "Биржа", example = "binance")
    private String exchange;

    @Schema(description = "Торговая пара", example = "BTCUSDT")
    private String symbol;

    @Schema(description = "Интервал", example = "1h")
    private String interval;

    @Schema(description = "Начало диапазона", example = "2024-01-01T00:00:00Z")
    private String from;

    @Schema(description = "Конец диапазона", example = "2024-01-31T23:59:59Z")
    private String to;

    @Schema(
            description = "Параметры стратегии",
            example = "{\"period\": 14}"
    )
    private Map<String, Object> params;

    @Schema(
            description = "Результирующие метрики",
            example = "{\"profit\": 1234.56}"
    )
    private Map<String, Object> metrics;

    @Schema(description = "Сообщение об ошибке", example = "Execution timeout")
    private String errorMessage;

    @Schema(
            description = "Дата создания",
            example = "2024-01-01T00:00:00Z",
            type = "string",
            format = "date-time"
    )
    private Instant createdAt;

    @Schema(
            description = "Дата завершения",
            example = "2024-01-01T01:00:00Z",
            type = "string",
            format = "date-time"
    )
    private Instant finishedAt;
}