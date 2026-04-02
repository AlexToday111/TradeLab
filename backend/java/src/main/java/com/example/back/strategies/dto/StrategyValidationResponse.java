package com.example.back.strategies.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Результат валидации стратегии")
public class StrategyValidationResponse {

    @Schema(description = "Признак валидности стратегии", example = "true")
    private Boolean valid;

    @Schema(description = "Название стратегии", example = "RSI Strategy")
    private String name;

    @Schema(
            description = "Схема параметров стратегии",
            example = "{\"period\":{\"type\":\"integer\",\"default\":14}}"
    )
    private Map<String, Object> parametersSchema;

    @Schema(description = "Ошибка валидации", example = "Function run(data, params) not found")
    private String error;
}