package com.example.back.runs.artifacts.repository;

import com.example.back.runs.artifacts.entity.RunArtifactEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunArtifactRepository extends JpaRepository<RunArtifactEntity, Long> {

    List<RunArtifactEntity> findAllByRunIdOrderByCreatedAtAsc(Long runId);

    Optional<RunArtifactEntity> findByIdAndRunId(Long id, Long runId);

    void deleteByRunId(Long runId);
}
