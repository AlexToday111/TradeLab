package com.example.back.runs.repository;

import com.example.back.runs.entity.RunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunRepository extends JpaRepository<RunEntity, Long> {
}
