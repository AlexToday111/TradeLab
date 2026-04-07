package com.example.back.backtest.model;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "backtest_trades")
@Getter
@Setter
public class BacktestTradeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false)
    private Long runId;

    @Column(name = "entry_time")
    private Instant entryTime;

    @Column(name = "exit_time")
    private Instant exitTime;

    @Column(name = "entry_price", nullable = false)
    private double entryPrice;

    @Column(name = "exit_price", nullable = false)
    private double exitPrice;

    @Column(nullable = false)
    private double quantity;

    @Column(nullable = false)
    private double pnl;

    @Column(nullable = false)
    private double fee;
}
