package com.example.back.runs.artifacts.dto;

import com.example.back.runs.artifacts.entity.RunArtifactEntity.ArtifactType;
import java.time.Instant;
import lombok.Builder;

@Builder
public record RunArtifactResponse(
        Long id,
        Long runId,
        ArtifactType artifactType,
        String artifactName,
        String contentType,
        String storagePath,
        Long sizeBytes,
        Instant createdAt
) {
}
