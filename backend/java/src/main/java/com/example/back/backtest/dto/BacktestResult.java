package com.example.back.backtest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BacktestResult {
    private Map<String, Object> summary;
    private List<BacktestTrade> trades;

    @JsonProperty("equity_curve")
    private List<EquityPoint> equityCurve;

    private List<String> logs;
    private List<String> warnings;
}
