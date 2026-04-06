package com.example.back.candles.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.back.candles.entity.CandleEntity;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:candles;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=INTERVAL",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
    }
)
class CandleRepositoryTest {

    @Autowired
    private CandleRepository candleRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findsCandlesByMarketAndRangeOrderedByOpenTime() {
        entityManager.persist(candle("binance", "BTCUSDT", "1h", "2024-01-01T00:00:00Z"));
        entityManager.persist(candle("binance", "BTCUSDT", "1h", "2024-01-01T01:00:00Z"));
        entityManager.persist(candle("binance", "ETHUSDT", "1h", "2024-01-01T00:00:00Z"));
        entityManager.flush();

        var candles = candleRepository
            .findByExchangeAndSymbolAndIntervalAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualOrderByOpenTimeAsc(
                "binance",
                "BTCUSDT",
                "1h",
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T02:00:00Z")
            );

        assertThat(candles).hasSize(2);
        assertThat(candles.get(0).getOpenTime()).isBefore(candles.get(1).getOpenTime());
    }

    private CandleEntity candle(String exchange, String symbol, String interval, String openTime) {
        Instant open = Instant.parse(openTime);
        return new CandleEntity(
            null,
            exchange,
            symbol,
            interval,
            open,
            open.plusSeconds(3600),
            new BigDecimal("1.0"),
            new BigDecimal("2.0"),
            new BigDecimal("0.5"),
            new BigDecimal("1.5"),
            new BigDecimal("10.0")
        );
    }
}
