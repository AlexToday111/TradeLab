package com.example.back.papertrading.dto;

import com.example.back.papertrading.entity.PaperSessionStatus;
import java.math.BigDecimal;

public record PaperSummaryResponse(
        Long sessionId,
        PaperSessionStatus status,
        BigDecimal initialBalance,
        BigDecimal currentBalance,
        BigDecimal realizedPnl,
        BigDecimal unrealizedPnl,
        BigDecimal equity,
        int openPositions,
        long ordersCount,
        long fillsCount
) {
}
