package com.example.back.strategies.repository;

import com.example.back.strategies.entity.StrategyVersionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StrategyVersionRepository extends JpaRepository<StrategyVersionEntity, Long> {

    List<StrategyVersionEntity> findAllByStrategyIdOrderByCreatedAtDesc(Long strategyId);

    Optional<StrategyVersionEntity> findByStrategyIdAndVersion(Long strategyId, String version);

    Optional<StrategyVersionEntity> findFirstByStrategyIdOrderByCreatedAtDesc(Long strategyId);

    @Query("""
            select version
            from StrategyVersionEntity version
            join StrategyFileEntity strategy on strategy.id = version.strategyId
            where version.id = :versionId and strategy.userId = :userId
            """)
    Optional<StrategyVersionEntity> findOwnedById(
            @Param("versionId") Long versionId,
            @Param("userId") Long userId
    );

    @Query("""
            select version
            from StrategyVersionEntity version
            join StrategyFileEntity strategy on strategy.id = version.strategyId
            where version.strategyId = :strategyId and strategy.userId = :userId
            order by version.createdAt desc
            """)
    List<StrategyVersionEntity> findAllOwnedByStrategyId(
            @Param("strategyId") Long strategyId,
            @Param("userId") Long userId
    );
}
