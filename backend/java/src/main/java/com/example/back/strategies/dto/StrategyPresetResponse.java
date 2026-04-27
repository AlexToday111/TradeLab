package com.example.back.strategies.dto;

import com.example.back.strategies.entity.StrategyParameterPresetEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record StrategyPresetResponse(
        Long id,
        Long strategyId,
        Long userId,
        String name,
        Map<String, Object> presetPayload,
        Instant createdAt,
        Instant updatedAt
) {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    public static StrategyPresetResponse fromEntity(StrategyParameterPresetEntity entity) {
        return new StrategyPresetResponse(
                entity.getId(),
                entity.getStrategyId(),
                entity.getUserId(),
                entity.getName(),
                readMap(entity.getPresetPayload()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private static Map<String, Object> readMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return OBJECT_MAPPER.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            log.error("Failed to parse strategy preset JSON map", e);
            return Map.of();
        }
    }
}
