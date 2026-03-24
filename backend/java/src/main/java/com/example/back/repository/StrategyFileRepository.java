package com.example.back.repository;

import com.example.back.entity.StrategyFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StrategyFileRepository extends JpaRepository<StrategyFileEntity, Long> {}
