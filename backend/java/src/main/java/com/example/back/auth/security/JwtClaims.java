package com.example.back.auth.security;

public record JwtClaims(
        Long userId,
        String email
) {
}
