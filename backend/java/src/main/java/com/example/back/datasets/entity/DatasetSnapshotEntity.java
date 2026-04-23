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
@Table(name = "dataset_snapshots")
public class DatasetSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dataset_id", nullable = false, length = 64)
    private String datasetId;

    @Column(name = "dataset_version", nullable = false, length = 128)
    private String datasetVersion;

    @Column(name = "source_exchange", length = 64)
    private String sourceExchange;

    @Column(length = 64)
    private String symbol;

    @Column(name = "\"interval\"", length = 32)
    private String interval;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "row_count")
    private Integer rowCount;

    @Column(length = 128)
    private String checksum;

    @Column(name = "source_metadata_json", columnDefinition = "TEXT")
    private String sourceMetadataJson;

    @Column(name = "coverage_metadata_json", columnDefinition = "TEXT")
    private String coverageMetadataJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
