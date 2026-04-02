package com.example.back.runs.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ Python сервиса на выполнение стратегии")
public class PythonRunExecuteResponse {

    @Schema(description = "Успешность выполнения", example = "true")
    private Boolean success;

    @Schema(
            description = "Метрики стратегии",
            example = "{\"profit\": 1234.56, \"sharpe\": 1.5}"
    )
    private Map<String, Object> metrics;

    @Schema(description = "Ошибка выполнения", example = "Strategy execution failed")
    private String error;
}