package com.example.back.papertrading.service;

import com.example.back.candles.repository.CandleRepository;
import com.example.back.papertrading.entity.PaperOrderEntity;
import com.example.back.papertrading.entity.PaperPositionEntity;
import com.example.back.papertrading.entity.PaperTradingSessionEntity;
import com.example.back.papertrading.repository.PaperPositionRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaperExchangeAdapter implements ExchangeAdapter {

    private final CandleRepository candleRepository;
    private final PaperPositionRepository paperPositionRepository;

    @Override
    public Optional<BigDecimal> getLatestPrice(PaperTradingSessionEntity session) {
        return candleRepository
                .findFirstByExchangeAndSymbolAndIntervalOrderByCloseTimeDesc(
                        session.getExchange(),
                        session.getSymbol(),
                        session.getTimeframe()
                )
                .map(candle -> candle.getClose());
    }

    @Override
    public PaperOrderEntity placeOrder(PaperTradingSessionEntity session, PaperOrderEntity order) {
        return order;
    }

    @Override
    public void cancelOrder(PaperOrderEntity order) {
        // Paper adapter has no external side effects.
    }

    @Override
    public PaperOrderEntity getOrder(PaperOrderEntity order) {
        return order;
    }

    @Override
    public Map<String, BigDecimal> getBalances(PaperTradingSessionEntity session) {
        return Map.of(session.getQuoteCurrency(), session.getCurrentBalance());
    }

    @Override
    public List<PaperPositionEntity> getPositions(PaperTradingSessionEntity session) {
        return paperPositionRepository.findAllBySessionIdOrderBySymbolAsc(session.getId());
    }
}
