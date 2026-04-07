package com.example.back.backtest.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.back.backtest.model.BacktestEquityPointEntity;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:backtest_equity;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
    }
)
class BacktestEquityPointRepositoryTest {

    @Autowired
    private BacktestEquityPointRepository backtestEquityPointRepository;

    @Test
    void returnsPointsOrderedByTimestamp() {
        BacktestEquityPointEntity first = new BacktestEquityPointEntity();
        first.setRunId(1L);
        first.setTimestamp(Instant.parse("2024-01-01T00:00:00Z"));
        first.setEquity(10_000.0);
        backtestEquityPointRepository.save(first);

        BacktestEquityPointEntity second = new BacktestEquityPointEntity();
        second.setRunId(1L);
        second.setTimestamp(Instant.parse("2024-01-01T01:00:00Z"));
        second.setEquity(10_100.0);
        backtestEquityPointRepository.save(second);

        List<BacktestEquityPointEntity> points =
            backtestEquityPointRepository.findByRunIdOrderByTimestampAsc(1L);

        assertThat(points).hasSize(2);
        assertThat(points.get(0).getTimestamp()).isBefore(points.get(1).getTimestamp());
    }
}
