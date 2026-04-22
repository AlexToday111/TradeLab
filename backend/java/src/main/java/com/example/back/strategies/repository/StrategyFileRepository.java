package com.example.back.strategies.repository;

import com.example.back.strategies.entity.StrategyFileEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StrategyFileRepository extends JpaRepository<StrategyFileEntity, Long> {

    List<StrategyFileEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<StrategyFileEntity> findByIdAndUserId(Long id, Long userId);
}
