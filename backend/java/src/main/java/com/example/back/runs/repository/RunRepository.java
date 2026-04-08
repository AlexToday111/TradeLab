package com.example.back.runs.repository;

import com.example.back.backtest.model.BacktestStatus;
import com.example.back.runs.entity.RunEntity;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RunRepository extends JpaRepository<RunEntity, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update RunEntity run
            set run.status = :status,
                run.errorMessage = :errorMessage,
                run.finishedAt = :finishedAt
            where run.id = :runId
            """)
    int updateFailureState(
            @Param("runId") Long runId,
            @Param("status") BacktestStatus status,
            @Param("errorMessage") String errorMessage,
            @Param("finishedAt") Instant finishedAt
    );
}
