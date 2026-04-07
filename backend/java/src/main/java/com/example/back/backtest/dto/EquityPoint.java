package com.example.back.backtest.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class EquityPoint {
    private Instant timestamp;
    private double equity;
}
