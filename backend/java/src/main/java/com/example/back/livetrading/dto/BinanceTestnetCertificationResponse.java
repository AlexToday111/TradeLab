package com.example.back.livetrading.dto;

import java.time.Instant;

public record BinanceTestnetCertificationResponse(
        String exchange,
        boolean testnetOnly,
        boolean realOrderSubmissionEnabled,
        boolean credentialsPresent,
        boolean credentialsValid,
        boolean accountSnapshotReachable,
        boolean openOrdersSnapshotReachable,
        boolean certified,
        String accountSnapshotSummary,
        String openOrdersSnapshotSummary,
        String message,
        Instant checkedAt
) {
}
