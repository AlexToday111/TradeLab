package com.example.back.backtest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EquityPoint {
    private Instant timestamp;
    private double equity;
    private double cash;

    @JsonProperty("position_size")
    private double positionSize;
}
