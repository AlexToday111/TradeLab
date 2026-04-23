package com.example.back.datasets.dto;

import com.example.back.datasets.entity.DatasetQualityReportEntity.QualityStatus;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import lombok.Builder;

@Builder
public record DatasetQualityReportResponse(
        Long id,
        String datasetId,
        Long snapshotId,
        QualityStatus qualityStatus,
        JsonNode issues,
        Instant checkedAt
) {
}
