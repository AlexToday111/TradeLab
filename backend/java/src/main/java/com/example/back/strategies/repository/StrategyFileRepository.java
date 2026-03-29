package com.example.back.strategies.repository;

import com.example.back.strategies.entity.StrategyFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StrategyFileRepository extends JpaRepository<StrategyFileEntity, Long> {}
