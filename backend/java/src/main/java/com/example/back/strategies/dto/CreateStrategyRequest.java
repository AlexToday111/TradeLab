package com.example.back.strategies.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

public record CreateStrategyRequest(
        @NotBlank
        String name,
        String strategyKey,
        String description,
        String strategyType,
        Map<String, Object> metadata,
        List<String> tags,
        Long templateId
) {
}
