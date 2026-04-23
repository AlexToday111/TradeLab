package com.example.back.datasets.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

@Builder
public record DatasetDetailsResponse(
        JsonNode dataset,
        DatasetSnapshotResponse latestSnapshot,
        DatasetQualityReportResponse latestQualityReport
) {
}
