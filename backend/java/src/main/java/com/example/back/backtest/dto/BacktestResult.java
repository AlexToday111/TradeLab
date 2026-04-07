package com.example.back.backtest.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class BacktestResult {
    private Map<String, Object> summary;
    private List<BacktestTrade> trades;
    private List<EquityPoint> equityCurve;

    private List<String> logs;
    private List<String> warnings;
}
