package com.example.back.datasets.repository;

import com.example.back.datasets.entity.DatasetSnapshotEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetSnapshotRepository extends JpaRepository<DatasetSnapshotEntity, Long> {

    List<DatasetSnapshotEntity> findAllByDatasetIdAndUserIdOrderByCreatedAtDesc(String datasetId, Long userId);

    Optional<DatasetSnapshotEntity> findFirstByDatasetIdAndUserIdOrderByCreatedAtDesc(String datasetId, Long userId);

    Optional<DatasetSnapshotEntity> findByIdAndUserId(Long id, Long userId);
}
