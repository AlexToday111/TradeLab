package com.example.back.strategies.repository;

import com.example.back.strategies.entity.StrategyParameterPresetEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StrategyParameterPresetRepository extends JpaRepository<StrategyParameterPresetEntity, Long> {

    List<StrategyParameterPresetEntity> findAllByStrategyIdAndUserIdOrderByCreatedAtDesc(Long strategyId, Long userId);

    Optional<StrategyParameterPresetEntity> findByIdAndUserId(Long id, Long userId);

    Optional<StrategyParameterPresetEntity> findByIdAndStrategyIdAndUserId(Long id, Long strategyId, Long userId);
}
