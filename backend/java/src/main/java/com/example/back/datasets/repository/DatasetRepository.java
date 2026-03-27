package com.example.back.datasets.repository;

import com.example.back.datasets.entity.DatasetEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetRepository extends JpaRepository<DatasetEntity, String> {

    List<DatasetEntity> findAllByOrderByCreatedAtDesc();
}
