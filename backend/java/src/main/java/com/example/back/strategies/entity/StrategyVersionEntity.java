package com.example.back.strategies.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "strategy_versions",
        uniqueConstraints = {
                @UniqueConstraint(name = "strategy_versions_strategy_version_key", columnNames = {"strategy_id", "version"}),
                @UniqueConstraint(name = "strategy_versions_strategy_checksum_key", columnNames = {"strategy_id", "checksum"})
        }
)
public class StrategyVersionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "strategy_id", nullable = false)
    private Long strategyId;

    @Column(nullable = false, length = 128)
    private String version;

    @Column(name = "file_path", nullable = false, columnDefinition = "TEXT")
    private String filePath;

    @Column(name = "filename", nullable = false)
    private String fileName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(nullable = false, length = 128)
    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", nullable = false)
    private ValidationStatus validationStatus;

    @Column(name = "validation_report", columnDefinition = "TEXT")
    private String validationReport;

    @Column(name = "parameters_schema_json", columnDefinition = "TEXT")
    private String parametersSchemaJson;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    @Column(name = "execution_engine_version", length = 128)
    private String executionEngineVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (validationStatus == null) {
            validationStatus = ValidationStatus.PENDING;
        }
    }

    public enum ValidationStatus {
        PENDING, VALID, WARNING, INVALID
    }
}
