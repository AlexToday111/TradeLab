package com.example.back.backtest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BacktestTrade {
    @JsonProperty("entry_time")
    private Instant entryTime;

    @JsonProperty("exit_time")
    private Instant exitTime;

    @JsonProperty("entry_price")
    private double entryPrice;

    @JsonProperty("exit_price")
    private double exitPrice;

    @JsonProperty("qty")
    private double quantity;

    private double pnl;
    private double fee;
}
