package com.example.back.livetrading.repository;

import com.example.back.livetrading.entity.CircuitBreakerStateEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CircuitBreakerStateRepository extends JpaRepository<CircuitBreakerStateEntity, Long> {

    Optional<CircuitBreakerStateEntity> findByUserIdAndExchange(Long userId, String exchange);

    List<CircuitBreakerStateEntity> findAllByUserIdOrderByUpdatedAtDesc(Long userId);
}
