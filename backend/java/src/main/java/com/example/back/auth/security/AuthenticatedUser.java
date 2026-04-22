package com.example.back.auth.security;

public record AuthenticatedUser(
        Long id,
        String email
) {
}
