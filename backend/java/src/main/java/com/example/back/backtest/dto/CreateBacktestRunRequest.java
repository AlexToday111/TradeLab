package com.example.back.backtest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Schema(description = "Запрос на запуск бэктеста")
public class CreateBacktestRunRequest {

    @NotNull
    @Schema(description = "ID стратегии", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long strategyId;

    @Schema(description = "ID конкретной версии стратегии", example = "101")
    private Long strategyVersionId;

    @Schema(description = "ID сохраненного набора параметров", example = "7")
    private Long parameterPresetId;

    @Schema(description = "Человекочитаемое имя запуска", example = "EMA BTCUSDT Jan 2024")
    private String runName;

    @NotBlank
    @Schema(description = "Биржа", example = "binance", requiredMode = Schema.RequiredMode.REQUIRED)
    private String exchange;

    @NotBlank
    @Schema(description = "Инструмент", example = "BTCUSDT", requiredMode = Schema.RequiredMode.REQUIRED)
    private String symbol;

    @NotBlank
    @Schema(description = "Таймфрейм", example = "1h", requiredMode = Schema.RequiredMode.REQUIRED)
    private String interval;

    @NotNull
    @Schema(
            description = "Начало диапазона",
            example = "2024-01-01T00:00:00Z",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Instant from;

    @NotNull
    @Schema(
            description = "Конец диапазона",
            example = "2024-01-03T00:00:00Z",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Instant to;

    @NotNull
    @Schema(
            description = "Параметры стратегии",
            example = "{\"fastPeriod\": 10, \"slowPeriod\": 21}",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Map<String, Object> params = new LinkedHashMap<>();

    @NotNull
    @DecimalMin(value = "0.00000001")
    @Schema(description = "Начальный капитал", example = "10000.0")
    private Double initialCash = 10_000.0;

    @NotNull
    @DecimalMin(value = "0.0")
    @Schema(description = "Комиссия на сделку", example = "0.001")
    private Double feeRate = 0.0;

    @NotNull
    @DecimalMin(value = "0.0")
    @Schema(description = "Проскальзывание в bps", example = "5.0")
    private Double slippageBps = 0.0;

    @NotNull
    @Schema(description = "Строгая валидация данных", example = "true")
    private Boolean strictData = Boolean.TRUE;

    @Schema(description = "Режим сайзинга позиции", example = "UNSPECIFIED")
    private String positionSizingMode = "UNSPECIFIED";
}
