package com.example.back.livetrading.dto;

import java.time.Instant;

public record LiveCredentialStatusResponse(
        Long id,
        String exchange,
        String keyReference,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
