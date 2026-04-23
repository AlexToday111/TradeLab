package com.example.back.datasets.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dataset_quality_reports")
public class DatasetQualityReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dataset_id", nullable = false, length = 64)
    private String datasetId;

    @Column(name = "dataset_snapshot_id")
    private Long datasetSnapshotId;

    @Column(name = "quality_status", nullable = false, length = 32)
    private String qualityStatus;

    @Column(name = "issues_json", nullable = false, columnDefinition = "TEXT")
    private String issuesJson;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;

    @PrePersist
    void onCreate() {
        if (checkedAt == null) {
            checkedAt = Instant.now();
        }
    }
}
