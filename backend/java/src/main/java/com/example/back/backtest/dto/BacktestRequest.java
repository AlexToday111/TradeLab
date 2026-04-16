package com.example.back.backtest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BacktestRequest {

    @NotBlank
    @JsonProperty("strategy_path")
    private String strategyPath;

    @NotBlank
    @JsonProperty("data_path")
    private String dataPath;

    @JsonProperty("strategy_params")
    private Map<String, Object> strategyParams;

    @Positive
    @JsonProperty("initial_cash")
    private double initialCash = 10_000.0;

    @JsonProperty("fee_rate")
    private double feeRate;

    @JsonProperty("slippage_bps")
    private double slippageBps;

    @JsonProperty("strict_data")
    private boolean strictData = true;

    @JsonProperty("run_id")
    private String runId;

    @JsonProperty("correlation_id")
    private String correlationId;
}
