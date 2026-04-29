package com.example.back.livetrading.repository;

import com.example.back.livetrading.entity.LiveExchangeCredentialEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiveExchangeCredentialRepository extends JpaRepository<LiveExchangeCredentialEntity, Long> {

    List<LiveExchangeCredentialEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<LiveExchangeCredentialEntity> findFirstByUserIdAndExchangeAndActiveTrueOrderByUpdatedAtDesc(
            Long userId,
            String exchange
    );
}
