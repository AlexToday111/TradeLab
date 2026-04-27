package com.example.back.strategies.dto;

import com.example.back.strategies.entity.StrategyFileEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Schema(description = "Информация о стратегии")
public record StrategyResponse(

        @Schema(description = "ID стратегии", example = "1")
        Long id,

        @Schema(description = "ID владельца", example = "42")
        Long ownerId,

        @Schema(description = "Стабильный ключ стратегии", example = "ema-cross")
        String strategyKey,

        @Schema(description = "Название стратегии", example = "RSI Strategy")
        String name,

        @Schema(description = "Описание стратегии")
        String description,

        @Schema(description = "Тип стратегии", example = "BACKTEST")
        String strategyType,

        @Schema(description = "Lifecycle-статус стратегии", example = "ACTIVE")
        StrategyFileEntity.StrategyLifecycleStatus lifecycleStatus,

        @Schema(description = "Последняя версия стратегии", example = "1")
        String latestVersion,

        @Schema(description = "ID последней версии стратегии", example = "10")
        Long latestVersionId,

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

        @Schema(description = "Metadata стратегии")
        Map<String, Object> metadata,

        @Schema(description = "Теги стратегии")
        List<String> tags,

        @Schema(description = "Content-Type последнего файла", example = "text/x-python")
        String contentType,

        @Schema(description = "Размер последнего файла в байтах", example = "1024")
        Long sizeBytes,

        @Schema(description = "SHA-256 checksum последнего файла")
        String checksum,

        @Schema(description = "Дата загрузки последнего файла")
        Instant uploadedAt,

        @Schema(
                description = "Дата создания",
                example = "2024-01-01T00:00:00Z",
                type = "string",
                format = "date-time"
        )
        Instant createdAt,

        @Schema(description = "Дата обновления")
        Instant updatedAt
) {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    public static StrategyResponse fromEntity(StrategyFileEntity entity) {
        return new StrategyResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getStrategyKey(),
                entity.getName(),
                entity.getDescription(),
                entity.getStrategyType(),
                entity.getLifecycleStatus(),
                entity.getLatestVersion(),
                entity.getLatestVersionId(),
                entity.getFileName(),
                entity.getStatus(),
                entity.getValidationError(),
                readMap(entity.getParametersSchemaJson()),
                readMap(entity.getMetadataJson()),
                readStringList(entity.getTagsJson()),
                entity.getContentType(),
                entity.getSizeBytes(),
                entity.getChecksum(),
                entity.getUploadedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private static Map<String, Object> readMap(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            log.error("Failed to parse strategy JSON map", e);
            return Map.of();
        }
    }

    private static List<String> readStringList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, STRING_LIST_TYPE);
        } catch (Exception e) {
            log.error("Failed to parse strategy tags JSON", e);
            return List.of();
        }
    }
}
