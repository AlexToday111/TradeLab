package com.example.back.papertrading.repository;

import com.example.back.papertrading.entity.PaperPositionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaperPositionRepository extends JpaRepository<PaperPositionEntity, Long> {

    List<PaperPositionEntity> findAllBySessionIdOrderBySymbolAsc(Long sessionId);

    Optional<PaperPositionEntity> findBySessionIdAndSymbol(Long sessionId, String symbol);
}
