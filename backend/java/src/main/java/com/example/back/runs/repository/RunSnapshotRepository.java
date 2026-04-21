package com.example.back.runs.repository;

import com.example.back.runs.entity.RunSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunSnapshotRepository extends JpaRepository<RunSnapshotEntity, Long> {
}
