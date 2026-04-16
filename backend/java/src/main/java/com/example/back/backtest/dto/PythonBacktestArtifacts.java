package com.example.back.backtest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PythonBacktestArtifacts {
    private String outputDir;
    private String equityCurvePath;
    private String tradesPath;
    private String summaryPath;
    private String logsPath;
    private String warningsPath;
    private Integer tradesCount;
    private Integer equityPointCount;
}
