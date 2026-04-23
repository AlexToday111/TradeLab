package com.example.back.datasets.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import lombok.Builder;

@Builder
public record DatasetSnapshotResponse(
        Long id,
        String datasetId,
        String datasetVersion,
        String sourceExchange,
        String symbol,
        String timeframe,
        Instant startTime,
        Instant endTime,
        Integer rowCount,
        String checksum,
        JsonNode sourceMetadata,
        JsonNode coverageMetadata,
        Instant createdAt
) {
}
