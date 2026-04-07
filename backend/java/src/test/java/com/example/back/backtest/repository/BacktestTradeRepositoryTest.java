package com.example.back.backtest.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.back.backtest.model.BacktestTradeEntity;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:backtest_trades;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
    }
)
class BacktestTradeRepositoryTest {

    @Autowired
    private BacktestTradeRepository backtestTradeRepository;

    @Test
    void findsTradesByRunId() {
        BacktestTradeEntity trade = new BacktestTradeEntity();
        trade.setRunId(1L);
        trade.setEntryTime(Instant.parse("2024-01-01T00:00:00Z"));
        trade.setExitTime(Instant.parse("2024-01-01T01:00:00Z"));
        trade.setEntryPrice(100.0);
        trade.setExitPrice(110.0);
        trade.setQuantity(1.0);
        trade.setPnl(10.0);
        trade.setFee(0.1);
        backtestTradeRepository.save(trade);

        assertThat(backtestTradeRepository.findByRunId(1L)).hasSize(1);
    }
}
