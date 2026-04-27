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

    public StrategyValidationResponse(
            Boolean valid,
            String name,
            Map<String, Object> parametersSchema,
            String error
    ) {
        this.valid = valid;
        this.name = name;
        this.parametersSchema = parametersSchema;
        this.error = error;
    }

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

    @Schema(description = "Persisted validation status", example = "VALID")
    private String validationStatus;

    @Schema(description = "Detailed validation report")
    private Map<String, Object> validationReport;

    @Schema(description = "Strategy metadata resolved by validator")
    private Map<String, Object> metadata;

    @Schema(description = "Execution engine version used by validation")
    private String engineVersion;
}
