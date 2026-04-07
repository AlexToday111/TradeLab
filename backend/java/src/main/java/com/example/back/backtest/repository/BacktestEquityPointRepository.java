package com.example.back.backtest.repository;

import com.example.back.backtest.model.BacktestEquityPointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BacktestEquityPointRepository extends JpaRepository<BacktestEquityPointEntity, Long> {
    void deleteByRunId(Long runId);

    List<BacktestEquityPointEntity> findByRunIdOrderByTimestampAsc(Long runId);
}
