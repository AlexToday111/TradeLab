package com.example.back.datasets.repository;

import com.example.back.datasets.entity.DatasetEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetRepository extends JpaRepository<DatasetEntity, String> {

    List<DatasetEntity> findAllByOrderByCreatedAtDesc();

    Optional<DatasetEntity> findFirstBySourceIgnoreCaseAndSymbolIgnoreCaseAndIntervalAndStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByImportedAtDesc(
            String source,
            String symbol,
            String interval,
            Instant startAt,
            Instant endAt
    );
}
