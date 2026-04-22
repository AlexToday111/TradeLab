package com.example.back.auth.dto;

import java.time.Instant;

public record AuthResponse(
        String token,
        UserResponse user
) {

    public record UserResponse(
            Long id,
            String email,
            Instant createdAt
    ) {
    }
}
