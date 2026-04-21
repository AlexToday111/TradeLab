package com.example.back.backtest.dto;

import com.example.back.backtest.model.BacktestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;
import lombok.Builder;

@Builder
@Schema(description = "Состояние и результат запуска бэктеста")
public record BacktestRunResponse(
        @Schema(description = "ID запуска", example = "101")
        Long runId,
        @Schema(description = "ID стратегии", example = "42")
        Long strategyId,
        @Schema(description = "Имя стратегии на момент запуска", example = "EMA")
        String strategyName,
        @Schema(description = "Идентификатор датасета, использованного в запуске", example = "dataset-binance-btcusdt-1h-v1")
        String datasetId,
        @Schema(description = "Correlation/run identifier for logs", example = "run-101")
        String correlationId,
        @Schema(description = "Статус бэктеста", example = "SUCCEEDED")
        BacktestStatus status,
        @Schema(description = "Биржа", example = "binance")
        String exchange,
        @Schema(description = "Инструмент", example = "BTCUSDT")
        String symbol,
        @Schema(description = "Таймфрейм", example = "1h")
        String interval,
        @Schema(description = "Начало диапазона", example = "2024-01-01T00:00:00Z")
        Instant from,
        @Schema(description = "Конец диапазона", example = "2024-01-03T00:00:00Z")
        Instant to,
        @Schema(description = "Параметры стратегии", example = "{\"fastPeriod\": 10}")
        Map<String, Object> params,
        @Schema(description = "Полная сохранённая конфигурация запуска", example = "{\"initialCash\":10000.0}")
        Map<String, Object> config,
        @Schema(description = "Сводка результата", example = "{\"profit\": 12.5, \"sharpe\": 1.3}")
        Map<String, Object> summary,
        @Schema(description = "Артефакты запуска и их metadata", example = "{\"tradesCount\": 12}")
        Map<String, Object> artifacts,
        @Schema(description = "Сообщение об ошибке", example = "Python process failed with exit code 1")
        String errorMessage,
        @Schema(description = "Время создания", example = "2024-01-01T00:00:00Z")
        Instant createdAt,
        @Schema(description = "Время старта", example = "2024-01-01T00:00:02Z")
        Instant startedAt,
        @Schema(description = "Время завершения", example = "2024-01-01T00:00:12Z")
        Instant finishedAt
) {
}
