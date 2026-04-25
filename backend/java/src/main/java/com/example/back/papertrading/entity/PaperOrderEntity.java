package com.example.back.papertrading.entity;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "paper_orders")
public class PaperOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaperOrderSide side;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaperOrderType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaperOrderStatus status;

    @Column(nullable = false, precision = 28, scale = 8)
    private BigDecimal quantity;

    @Column(precision = 28, scale = 8)
    private BigDecimal price;

    @Column(name = "filled_quantity", nullable = false, precision = 28, scale = 8)
    private BigDecimal filledQuantity;

    @Column(name = "average_fill_price", precision = 28, scale = 8)
    private BigDecimal averageFillPrice;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "filled_at")
    private Instant filledAt;

    @Column(name = "rejected_reason", columnDefinition = "TEXT")
    private String rejectedReason;

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
            status = PaperOrderStatus.NEW;
        }
        if (filledQuantity == null) {
            filledQuantity = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
