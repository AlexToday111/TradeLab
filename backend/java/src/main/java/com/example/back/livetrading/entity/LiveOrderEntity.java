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
@Table(name = "live_orders")
public class LiveOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "strategy_id")
    private Long strategyId;

    @Column(name = "strategy_version_id")
    private Long strategyVersionId;

    @Column(nullable = false)
    private String exchange;

    @Column(nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LiveOrderSide side;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LiveOrderType type;

    @Column(nullable = false, precision = 28, scale = 8)
    private BigDecimal quantity;

    @Column(name = "requested_price", precision = 28, scale = 8)
    private BigDecimal requestedPrice;

    @Column(name = "executed_price", precision = 28, scale = 8)
    private BigDecimal executedPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LiveOrderStatus status;

    @Column(name = "exchange_order_id")
    private String exchangeOrderId;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "filled_at")
    private Instant filledAt;

    @Column(name = "rejected_reason", columnDefinition = "TEXT")
    private String rejectedReason;

    @Column(name = "source_run_id")
    private Long sourceRunId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (status == null) {
            status = LiveOrderStatus.CREATED;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
