package com.example.back.repository;

import com.example.back.entity.DatasetEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetRepository extends JpaRepository<DatasetEntity, String> {

    List<DatasetEntity> findAllByOrderByCreatedAtDesc();
}
