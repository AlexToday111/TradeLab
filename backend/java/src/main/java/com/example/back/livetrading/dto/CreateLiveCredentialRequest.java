package com.example.back.livetrading.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateLiveCredentialRequest(
        @NotBlank String exchange,
        @NotBlank String apiKey,
        @NotBlank String apiSecret,
        boolean active
) {
}
