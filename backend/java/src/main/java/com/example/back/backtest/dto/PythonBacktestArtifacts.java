package com.example.back.backtest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PythonBacktestArtifacts {
    private String equityCurvePath;
    private String tradesPath;
}
