package com.example.back.strategies.dto;

import com.example.back.strategies.entity.StrategyFileEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Schema(description = "Информация о стратегии")
public record StrategyResponse(

        @Schema(description = "ID стратегии", example = "1")
        Long id,

        @Schema(description = "Название стратегии", example = "RSI Strategy")
        String name,

        @Schema(description = "Имя файла стратегии", example = "rsi_strategy.py")
        String fileName,

        @Schema(description = "Статус стратегии", example = "VALID")
        StrategyFileEntity.StrategyStatus status,

        @Schema(description = "Ошибка валидации", example = "Missing required function: run")
        String validationError,

        @Schema(
                description = "Схема параметров стратегии",
                example = "{\"period\":{\"type\":\"integer\",\"default\":14}}"
        )
        Map<String, Object> parametersSchema,

        @Schema(
                description = "Дата создания",
                example = "2024-01-01T00:00:00Z",
                type = "string",
                format = "date-time"
        )
        Instant createdAt
) {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static StrategyResponse fromEntity(StrategyFileEntity entity) {
        Map<String, Object> parametersSchema = null;

        if (entity.getParametersSchemaJson() != null && !entity.getParametersSchemaJson().isEmpty()) {
            try {
                parametersSchema = objectMapper.readValue(
                        entity.getParametersSchemaJson(),
                        new TypeReference<Map<String, Object>>() {}
                );
            } catch (Exception e) {
                log.error("Failed to parse parametersSchemaJson", e);
                parametersSchema = Map.of();
            }
        }

        return new StrategyResponse(
                entity.getId(),
                entity.getName(),
                entity.getFileName(),
                entity.getStatus(),
                entity.getValidationError(),
                parametersSchema,
                entity.getCreatedAt()
        );
    }
}