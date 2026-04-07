package com.example.back.runs.entity;

import com.example.back.backtest.model.BacktestStatus;
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
@Table(name = "runs")
public class RunEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "strategy_id", nullable = false)
    private Long strategyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BacktestStatus status;

    @Column(nullable = false)
    private String symbol;

    @Column(name = "interval", nullable = false)
    private String interval;

    @Column(nullable = false)
    private String exchange;

    @Column(name = "date_from", nullable = false)
    private Instant dateFrom;

    @Column(name = "date_to", nullable = false)
    private Instant dateTo;

    @Column(name = "params_json")
    private String paramsJson;

    @Column(name = "metrics_json")
    private String metricsJson;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = BacktestStatus.PENDING;
        }
    }

}
