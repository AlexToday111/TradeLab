package com.example.back.executionjobs.dto;

import com.example.back.executionjobs.entity.ExecutionJobStatus;
import java.time.Instant;
import lombok.Builder;

@Builder
public record ExecutionJobResponse(
        Long id,
        Long runId,
        Long userId,
        ExecutionJobStatus status,
        Integer priority,
        Integer attemptCount,
        Integer maxAttempts,
        Instant queuedAt,
        Instant startedAt,
        Instant finishedAt,
        Instant lockedAt,
        String lockedBy,
        Boolean cancelRequested,
        String errorCode,
        String errorMessage,
        Instant createdAt,
        Instant updatedAt
) {
}
