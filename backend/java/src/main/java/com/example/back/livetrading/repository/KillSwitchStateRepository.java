package com.example.back.livetrading.repository;

import com.example.back.livetrading.entity.KillSwitchStateEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KillSwitchStateRepository extends JpaRepository<KillSwitchStateEntity, Long> {

    Optional<KillSwitchStateEntity> findByUserId(Long userId);
}
