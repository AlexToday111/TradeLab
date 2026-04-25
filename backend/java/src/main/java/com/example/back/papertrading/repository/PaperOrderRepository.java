package com.example.back.papertrading.repository;

import com.example.back.papertrading.entity.PaperOrderEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaperOrderRepository extends JpaRepository<PaperOrderEntity, Long> {

    List<PaperOrderEntity> findAllBySessionIdAndUserIdOrderByCreatedAtDesc(Long sessionId, Long userId);

    Optional<PaperOrderEntity> findByIdAndUserId(Long id, Long userId);
}
