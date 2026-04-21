package com.example.back.runs.dto;

import com.example.back.backtest.dto.BacktestTrade;
import com.example.back.backtest.dto.EquityPoint;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Builder;

@Builder
public record RunResultResponse(
        Long runId,
        RunStatusResponse status,
        String engineVersion,
        Instant startedAt,
        Instant finishedAt,
        Map<String, Object> summary,
        Map<String, Object> metrics,
        Map<String, Object> artifacts,
        List<BacktestTrade> trades,
        List<EquityPoint> equityCurve,
        String errorMessage
) {
}
