package com.example.back.runs.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

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
    private RunStatus status;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String interval;

    @Column(nullable = false)
    private String exchange;

    @Column(name = "date_from", nullable = false)
    private Instant dateFrom;

    @Column(name = "date_to", nullable = false)
    private Instant dateTo;

    @Column(name = "params_json")
    private String paramsJson;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "metrics_json")
    private String metricsJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "finished_at", nullable = false)
    private Instant finishedAt;

    public enum RunStatus {
        PENDING, RUNNING, COMPLETED, FAILED
    }

}
