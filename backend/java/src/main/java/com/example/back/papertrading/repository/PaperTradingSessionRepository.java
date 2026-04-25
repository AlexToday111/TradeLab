package com.example.back.papertrading.repository;

import com.example.back.papertrading.entity.PaperTradingSessionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaperTradingSessionRepository extends JpaRepository<PaperTradingSessionEntity, Long> {

    List<PaperTradingSessionEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<PaperTradingSessionEntity> findByIdAndUserId(Long id, Long userId);
}
