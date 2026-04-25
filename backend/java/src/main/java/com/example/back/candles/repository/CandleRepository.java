package com.example.back.candles.repository;

import com.example.back.candles.entity.CandleEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandleRepository extends JpaRepository<CandleEntity, Long> {

    List<CandleEntity> findByExchangeAndSymbolAndIntervalAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualOrderByOpenTimeAsc(
        String exchange,
        String symbol,
        String interval,
        Instant from,
        Instant to
    );

    Optional<CandleEntity> findFirstByExchangeAndSymbolAndIntervalOrderByCloseTimeDesc(
        String exchange,
        String symbol,
        String interval
    );
}
