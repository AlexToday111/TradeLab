package com.example.back.papertrading.repository;

import com.example.back.papertrading.entity.PaperFillEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaperFillRepository extends JpaRepository<PaperFillEntity, Long> {

    List<PaperFillEntity> findAllBySessionIdOrderByExecutedAtDesc(Long sessionId);
}
