package com.example.back.service;

import com.example.back.dto.CandleResponse;
import com.example.back.entity.CandleEntity;
import com.example.back.repository.CandleRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CandleQueryService {

    private final CandleRepository candleRepository;

    public CandleQueryService(CandleRepository candleRepository) {
        this.candleRepository = candleRepository;
    }

    @Transactional(readOnly = true)
    public List<CandleResponse> getCandles(
        String exchange,
        String symbol,
        String interval,
        Instant from,
        Instant to
    ) {
        return candleRepository
            .findByExchangeAndSymbolAndIntervalAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualOrderByOpenTimeAsc(
                exchange.trim().toLowerCase(),
                symbol.trim().toUpperCase(),
                interval.trim(),
                from,
                to
            )
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private CandleResponse toResponse(CandleEntity candle) {
        return new CandleResponse(
            candle.getExchange(),
            candle.getSymbol(),
            candle.getInterval(),
            candle.getOpenTime(),
            candle.getCloseTime(),
            candle.getOpen(),
            candle.getHigh(),
            candle.getLow(),
            candle.getClose(),
            candle.getVolume()
        );
    }
}
