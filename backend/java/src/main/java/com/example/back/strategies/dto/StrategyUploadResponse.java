package com.example.back.strategies.dto;

import com.example.back.strategies.entity.StrategyFileEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Результат загрузки стратегии")
public record StrategyUploadResponse(

        @Schema(description = "ID стратегии", example = "1")
        Long id,

        @Schema(description = "Название стратегии", example = "RSI Strategy")
        String name,

        @Schema(description = "Имя файла", example = "rsi_strategy.py")
        String fileName,

        @Schema(description = "Статус стратегии", example = "VALID")
        StrategyFileEntity.StrategyStatus status,

        @Schema(description = "Ошибка валидации", example = "Missing required function: run")
        String validationError,

        @Schema(
                description = "Список параметров стратегии",
                example = "[\"period\", \"threshold\"]"
        )
        List<String> parametersSchema
) {}