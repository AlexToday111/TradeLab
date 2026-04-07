package com.example.back.backtest.repository;

import com.example.back.backtest.model.BacktestRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BacktestRunRepository extends JpaRepository<BacktestRun, Long> {
}