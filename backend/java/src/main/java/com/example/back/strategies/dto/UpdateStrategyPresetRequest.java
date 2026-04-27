package com.example.back.strategies.dto;

import java.util.Map;

public record UpdateStrategyPresetRequest(
        String name,
        Map<String, Object> presetPayload
) {
}
