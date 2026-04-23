package com.example.back.datasets.repository;

import com.example.back.datasets.entity.DatasetQualityReportEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetQualityReportRepository extends JpaRepository<DatasetQualityReportEntity, Long> {

    List<DatasetQualityReportEntity> findAllByDatasetIdAndUserIdOrderByCheckedAtDesc(String datasetId, Long userId);

    Optional<DatasetQualityReportEntity> findFirstByDatasetIdAndUserIdOrderByCheckedAtDesc(String datasetId, Long userId);
}
