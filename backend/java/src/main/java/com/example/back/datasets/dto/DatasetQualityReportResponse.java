package com.example.back.datasets.dto;

import java.time.Instant;
import lombok.Builder;

@Builder
public record DatasetQualityReportResponse(
        Long id,
        String datasetId,
        Long datasetSnapshotId,
        String qualityStatus,
        Object issues,
        Instant checkedAt
) {
}
