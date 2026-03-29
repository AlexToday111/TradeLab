package com.example.back.strategies.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на валидацию стратегии")
public class StrategyValidationRequest {

    @NotBlank(message = "Путь к файлу не может быть пустым")
    @Schema(
            description = "Путь к файлу стратегии",
            example = "/opt/tradelab/strategies/rsi_strategy.py",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String filePath;
}