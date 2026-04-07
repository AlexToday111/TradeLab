package com.example.back.backtest.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class BacktestTrade {
    private Instant entryTime;
    private Instant exitTime;

    private double entryPrice;
    private double exitPrice;

    private double quantity;
    private double pnl;
    private double fee;
}
