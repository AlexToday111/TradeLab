package com.example.back.runs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "run_snapshots")
public class RunSnapshotEntity {
    @Id
    @Column(name = "run_id", nullable = false)
    private Long runId;

    @Column(name = "strategy_version", nullable = false, length = 128)
    private String strategyVersion;

    @Column(name = "dataset_version", nullable = false, length = 128)
    private String datasetVersion;

    @Column(name = "params_snapshot_json", nullable = false, columnDefinition = "TEXT")
    private String paramsSnapshotJson;

    @Column(name = "execution_config_snapshot_json", nullable = false, columnDefinition = "TEXT")
    private String executionConfigSnapshotJson;

    @Column(name = "market_assumptions_snapshot_json", nullable = false, columnDefinition = "TEXT")
    private String marketAssumptionsSnapshotJson;

    @Column(name = "engine_version", length = 128)
    private String engineVersion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
