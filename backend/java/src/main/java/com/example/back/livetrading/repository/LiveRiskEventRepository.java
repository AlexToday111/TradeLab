package com.example.back.livetrading.repository;

import com.example.back.livetrading.entity.LiveRiskEventEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiveRiskEventRepository extends JpaRepository<LiveRiskEventEntity, Long> {

    List<LiveRiskEventEntity> findTop50ByUserIdOrderByCreatedAtDesc(Long userId);
}
