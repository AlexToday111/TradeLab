package com.example.back.runs.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.back.backtest.model.BacktestStatus;
import com.example.back.runs.entity.RunEntity;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:runs;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=INTERVAL",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
    }
)
class RunRepositoryTest {

    @Autowired
    private RunRepository runRepository;

    @Test
    void savesAndLoadsRunEntity() {
        RunEntity entity = new RunEntity();
        entity.setStrategyId(1L);
        entity.setStatus(BacktestStatus.PENDING);
        entity.setExchange("binance");
        entity.setSymbol("BTCUSDT");
        entity.setInterval("1h");
        entity.setDateFrom(Instant.parse("2024-01-01T00:00:00Z"));
        entity.setDateTo(Instant.parse("2024-01-02T00:00:00Z"));
        entity.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));

        RunEntity saved = runRepository.save(entity);

        assertThat(runRepository.findById(saved.getId())).isPresent();
        assertThat(runRepository.findById(saved.getId()).orElseThrow().getStatus())
            .isEqualTo(BacktestStatus.PENDING);
    }
}
