package com.example.back.strategies.dto;

import com.example.back.strategies.entity.StrategyVersionEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record StrategyVersionResponse(
        Long id,
        Long strategyId,
        String version,
        String filePath,
        String fileName,
        String contentType,
        Long sizeBytes,
        String checksum,
        StrategyVersionEntity.ValidationStatus validationStatus,
        Map<String, Object> validationReport,
        Map<String, Object> parametersSchema,
        Map<String, Object> metadata,
        String executionEngineVersion,
        Instant createdAt,
        Long createdBy
) {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    public static StrategyVersionResponse fromEntity(StrategyVersionEntity entity) {
        return new StrategyVersionResponse(
                entity.getId(),
                entity.getStrategyId(),
                entity.getVersion(),
                entity.getFilePath(),
                entity.getFileName(),
                entity.getContentType(),
                entity.getSizeBytes(),
                entity.getChecksum(),
                entity.getValidationStatus(),
                readMap(entity.getValidationReport()),
                readMap(entity.getParametersSchemaJson()),
                readMap(entity.getMetadataJson()),
                entity.getExecutionEngineVersion(),
                entity.getCreatedAt(),
                entity.getCreatedBy()
        );
    }

    private static Map<String, Object> readMap(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            log.error("Failed to parse strategy version JSON map", e);
            return Map.of();
        }
    }
}
