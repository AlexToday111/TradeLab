package com.example.back.strategies.dto;

import com.example.back.strategies.entity.StrategyFileEntity;
import java.util.List;
import java.util.Map;

public record UpdateStrategyRequest(
        String name,
        String description,
        String strategyType,
        StrategyFileEntity.StrategyLifecycleStatus lifecycleStatus,
        Map<String, Object> metadata,
        List<String> tags
) {
}
