package com.example.back.livetrading.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;

@Data
@Entity
@Table(name = "live_positions")
public class LivePositionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String exchange;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false, precision = 28, scale = 8)
    private BigDecimal quantity;

    @Column(name = "average_entry_price", nullable = false, precision = 28, scale = 8)
    private BigDecimal averageEntryPrice;

    @Column(name = "realized_pnl", nullable = false, precision = 28, scale = 8)
    private BigDecimal realizedPnl;

    @Column(name = "unrealized_pnl", nullable = false, precision = 28, scale = 8)
    private BigDecimal unrealizedPnl;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false)
    private LivePositionSyncStatus syncStatus;

    @PrePersist
    void prePersist() {
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
        if (syncStatus == null) {
            syncStatus = LivePositionSyncStatus.STALE;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
