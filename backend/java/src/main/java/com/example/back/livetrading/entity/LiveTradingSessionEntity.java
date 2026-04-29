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
@Table(name = "live_trading_sessions")
public class LiveTradingSessionEntity {

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

    @Column(name = "base_currency", nullable = false)
    private String baseCurrency;

    @Column(name = "quote_currency", nullable = false)
    private String quoteCurrency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LiveSessionStatus status;

    @Column(name = "max_order_notional", nullable = false, precision = 28, scale = 8)
    private BigDecimal maxOrderNotional;

    @Column(name = "max_position_notional", nullable = false, precision = 28, scale = 8)
    private BigDecimal maxPositionNotional;

    @Column(name = "max_daily_notional", nullable = false, precision = 28, scale = 8)
    private BigDecimal maxDailyNotional;

    @Column(name = "symbol_whitelist", columnDefinition = "TEXT")
    private String symbolWhitelist;

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
            status = LiveSessionStatus.CREATED;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
