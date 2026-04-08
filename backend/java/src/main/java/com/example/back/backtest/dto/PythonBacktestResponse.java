package com.example.back.backtest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PythonBacktestResponse {
    private String status;
    private Map<String, Object> metrics;
    private String errorMessage;
    private PythonBacktestArtifacts artifacts;
}
