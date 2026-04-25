package com.example.back.papertrading.service;

import com.example.back.papertrading.dto.PaperFillResponse;
import com.example.back.papertrading.dto.PaperOrderResponse;
import com.example.back.papertrading.dto.PaperPositionResponse;
import com.example.back.papertrading.dto.PaperSessionResponse;
import com.example.back.papertrading.entity.PaperFillEntity;
import com.example.back.papertrading.entity.PaperOrderEntity;
import com.example.back.papertrading.entity.PaperPositionEntity;
import com.example.back.papertrading.entity.PaperTradingSessionEntity;
import org.springframework.stereotype.Component;

@Component
public class PaperTradingMapper {

    public PaperSessionResponse toSessionResponse(PaperTradingSessionEntity entity) {
        return new PaperSessionResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getExchange(),
                entity.getSymbol(),
                entity.getTimeframe(),
                entity.getStatus(),
                entity.getInitialBalance(),
                entity.getCurrentBalance(),
                entity.getBaseCurrency(),
                entity.getQuoteCurrency(),
                entity.getStartedAt(),
                entity.getStoppedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public PaperOrderResponse toOrderResponse(PaperOrderEntity entity) {
        return new PaperOrderResponse(
                entity.getId(),
                entity.getSessionId(),
                entity.getUserId(),
                entity.getSymbol(),
                entity.getSide(),
                entity.getType(),
                entity.getStatus(),
                entity.getQuantity(),
                entity.getPrice(),
                entity.getFilledQuantity(),
                entity.getAverageFillPrice(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getFilledAt(),
                entity.getRejectedReason()
        );
    }

    public PaperFillResponse toFillResponse(PaperFillEntity entity) {
        return new PaperFillResponse(
                entity.getId(),
                entity.getOrderId(),
                entity.getSessionId(),
                entity.getSymbol(),
                entity.getSide(),
                entity.getQuantity(),
                entity.getPrice(),
                entity.getFee(),
                entity.getFeeCurrency(),
                entity.getExecutedAt()
        );
    }

    public PaperPositionResponse toPositionResponse(PaperPositionEntity entity) {
        return new PaperPositionResponse(
                entity.getId(),
                entity.getSessionId(),
                entity.getSymbol(),
                entity.getQuantity(),
                entity.getAverageEntryPrice(),
                entity.getRealizedPnl(),
                entity.getUnrealizedPnl(),
                entity.getUpdatedAt()
        );
    }
}
