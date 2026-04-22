package com.example.back.runs.repository;

import com.example.back.runs.entity.RunEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

public interface RunRepository extends JpaRepository<RunEntity, Long> {

    List<RunEntity> findAllByUserId(Long userId, Pageable pageable);

    List<RunEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<RunEntity> findByIdAndUserId(Long id, Long userId);
}
