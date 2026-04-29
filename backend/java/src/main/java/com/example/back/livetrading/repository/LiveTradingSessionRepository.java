package com.example.back.livetrading.repository;

import com.example.back.livetrading.entity.LiveTradingSessionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiveTradingSessionRepository extends JpaRepository<LiveTradingSessionEntity, Long> {

    List<LiveTradingSessionEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<LiveTradingSessionEntity> findByIdAndUserId(Long id, Long userId);
}
