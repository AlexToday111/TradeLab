package com.example.back.backtest.repository;

import com.example.back.backtest.model.BacktestTradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BacktestTradeRepository extends JpaRepository<BacktestTradeEntity, Long> {
    List<BacktestTradeEntity> findByRunId(Long runId);
}