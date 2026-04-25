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
@Table(name = "paper_trading_sessions")
public class PaperTradingSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String exchange;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String timeframe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaperSessionStatus status;

    @Column(name = "initial_balance", nullable = false, precision = 28, scale = 8)
    private BigDecimal initialBalance;

    @Column(name = "current_balance", nullable = false, precision = 28, scale = 8)
    private BigDecimal currentBalance;

    @Column(name = "base_currency", nullable = false)
    private String baseCurrency;

    @Column(name = "quote_currency", nullable = false)
    private String quoteCurrency;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "stopped_at")
    private Instant stoppedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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
            status = PaperSessionStatus.CREATED;
        }
        if (currentBalance == null) {
            currentBalance = initialBalance;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
