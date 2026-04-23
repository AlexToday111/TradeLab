package com.example.back.artifacts.dto;

import java.time.Instant;
import lombok.Builder;

@Builder
public record RunArtifactMetadataResponse(
        Long id,
        Long runId,
        String artifactType,
        String artifactName,
        String contentType,
        String storagePath,
        Long sizeBytes,
        Instant createdAt
) {
}
