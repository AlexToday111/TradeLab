package com.example.back.strategies.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record CreateStrategyPresetRequest(
        @NotBlank
        String name,
        @NotNull
        Map<String, Object> presetPayload
) {
}
