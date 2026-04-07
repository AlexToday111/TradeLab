package com.example.back.backtest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ после создания запуска")
public record BacktestCreatedResponse(
        @Schema(description = "ID запуска", example = "101")
        Long runId
) {
}
