package com.example.back.backtest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.Map;

@Data
public class BacktestRequest {

    @NotNull
    private String strategyPath;

    @NotNull
    private String symbol;

    @NotNull
    private String timeframe;

    private String dateFrom;
    private String dateTo;

    private Map<String, Object> strategyParams;

    @Positive
    private double initialCapital;

    private double fee;
    private double slippage;
}