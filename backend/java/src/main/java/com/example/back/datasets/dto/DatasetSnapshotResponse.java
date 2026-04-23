package com.example.back.datasets.dto;

import java.time.Instant;
import lombok.Builder;

@Builder
public record DatasetSnapshotResponse(
        Long id,
        String datasetId,
        String datasetVersion,
        String sourceExchange,
        String symbol,
        String interval,
        Instant startTime,
        Instant endTime,
        Integer rowCount,
        String checksum,
        Object sourceMetadata,
        Object coverageMetadata,
        Instant createdAt
) {
}
