package com.example.back.papertrading.service;

import com.example.back.papertrading.entity.PaperOrderEntity;
import com.example.back.papertrading.entity.PaperPositionEntity;
import com.example.back.papertrading.entity.PaperTradingSessionEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ExchangeAdapter {

    Optional<BigDecimal> getLatestPrice(PaperTradingSessionEntity session);

    PaperOrderEntity placeOrder(PaperTradingSessionEntity session, PaperOrderEntity order);

    void cancelOrder(PaperOrderEntity order);

    PaperOrderEntity getOrder(PaperOrderEntity order);

    Map<String, BigDecimal> getBalances(PaperTradingSessionEntity session);

    List<PaperPositionEntity> getPositions(PaperTradingSessionEntity session);
}
