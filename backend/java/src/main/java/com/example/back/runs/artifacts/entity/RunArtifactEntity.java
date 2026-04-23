package com.example.back.runs.artifacts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "run_artifacts")
public class RunArtifactEntity {

    public enum ArtifactType {
        EQUITY_CURVE,
        TRADES,
        METRICS_JSON,
        SUMMARY_JSON,
        REPORT_JSON,
        EXPORT_CSV
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false)
    private Long runId;

    @Enumerated(EnumType.STRING)
    @Column(name = "artifact_type", nullable = false, length = 64)
    private ArtifactType artifactType;

    @Column(name = "artifact_name", nullable = false)
    private String artifactName;

    @Column(name = "content_type", nullable = false, length = 128)
    private String contentType;

    @Column(name = "storage_path", columnDefinition = "TEXT")
    private String storagePath;

    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
