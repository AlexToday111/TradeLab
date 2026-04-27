package com.example.back.strategies.dto;

import com.example.back.strategies.entity.StrategyTemplateEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record StrategyTemplateResponse(
        Long id,
        String templateKey,
        String name,
        String description,
        String strategyType,
        String category,
        Map<String, Object> defaultParameters,
        String templateReference,
        Map<String, Object> metadata,
        Instant createdAt
) {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    public static StrategyTemplateResponse fromEntity(StrategyTemplateEntity entity) {
        return new StrategyTemplateResponse(
                entity.getId(),
                entity.getTemplateKey(),
                entity.getName(),
                entity.getDescription(),
                entity.getStrategyType(),
                entity.getCategory(),
                readMap(entity.getDefaultParametersJson()),
                entity.getTemplateReference(),
                readMap(entity.getMetadataJson()),
                entity.getCreatedAt()
        );
    }

    private static Map<String, Object> readMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return OBJECT_MAPPER.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            log.error("Failed to parse strategy template JSON map", e);
            return Map.of();
        }
    }
}
