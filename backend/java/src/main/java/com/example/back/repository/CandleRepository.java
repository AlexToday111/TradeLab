package com.example.back.repository;

import com.example.back.entity.CandleEntity;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandleRepository extends JpaRepository<CandleEntity, Long> {

    List<CandleEntity> findByExchangeAndSymbolAndIntervalAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualOrderByOpenTimeAsc(
        String exchange,
        String symbol,
        String interval,
        Instant from,
        Instant to
    );
}
