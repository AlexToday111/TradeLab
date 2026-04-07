package com.example.back.backtest.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "backtest_equity_points")
@Getter
@Setter
public class BacktestEquityPointEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false)
    private Long runId;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private double equity;
}