package com.example.back.strategies.repository;

import com.example.back.strategies.entity.StrategyTemplateEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StrategyTemplateRepository extends JpaRepository<StrategyTemplateEntity, Long> {

    List<StrategyTemplateEntity> findAllByOrderByNameAsc();

    Optional<StrategyTemplateEntity> findByTemplateKey(String templateKey);
}
