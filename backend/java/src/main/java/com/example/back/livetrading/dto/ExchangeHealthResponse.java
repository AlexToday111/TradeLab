package com.example.back.livetrading.dto;

public record ExchangeHealthResponse(
        String exchange,
        boolean connected,
        boolean credentialsValid,
        boolean realOrderSubmissionEnabled,
        String message
) {
}
