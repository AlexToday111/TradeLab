package com.example.back.candles.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.back.candles.entity.CandleEntity;
import com.example.back.candles.repository.CandleRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CandleQueryServiceTest {

    @Mock
    private CandleRepository candleRepository;

    @InjectMocks
    private CandleQueryService candleQueryService;

    @Test
    void getCandlesNormalizesFiltersAndMapsEntities() {
        CandleEntity entity = new CandleEntity(
            1L,
            "binance",
            "BTCUSDT",
            "1h",
            Instant.parse("2024-01-01T00:00:00Z"),
            Instant.parse("2024-01-01T01:00:00Z"),
            new BigDecimal("1.0"),
            new BigDecimal("2.0"),
            new BigDecimal("0.5"),
            new BigDecimal("1.5"),
            new BigDecimal("10.0")
        );
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-01-02T00:00:00Z");
        when(
            candleRepository
                .findByExchangeAndSymbolAndIntervalAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualOrderByOpenTimeAsc(
                    "binance",
                    "BTCUSDT",
                    "1h",
                    from,
                    to
                )
        ).thenReturn(List.of(entity));

        var responses = candleQueryService.getCandles(" Binance ", " btcusdt ", "1h", from, to);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).symbol()).isEqualTo("BTCUSDT");
        assertThat(responses.get(0).open()).isEqualByComparingTo("1.0");
        verify(candleRepository)
            .findByExchangeAndSymbolAndIntervalAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualOrderByOpenTimeAsc(
                "binance",
                "BTCUSDT",
                "1h",
                from,
                to
            );
    }
}
