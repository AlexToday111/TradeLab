package com.example.back.executionjobs.repository;

import com.example.back.executionjobs.entity.ExecutionJobEntity;
import com.example.back.executionjobs.entity.ExecutionJobStatus;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExecutionJobRepository extends JpaRepository<ExecutionJobEntity, Long> {

    List<ExecutionJobEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<ExecutionJobEntity> findByIdAndUserId(Long id, Long userId);

    Optional<ExecutionJobEntity> findFirstByRunIdAndUserIdOrderByCreatedAtDesc(Long runId, Long userId);

    List<ExecutionJobEntity> findAllByRunIdAndUserIdOrderByCreatedAtDesc(Long runId, Long userId);

    boolean existsByRunIdAndStatusIn(Long runId, Collection<ExecutionJobStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select job
            from ExecutionJobEntity job
            where job.status in :statuses
            order by job.priority desc, job.queuedAt asc, job.id asc
            """)
    List<ExecutionJobEntity> findClaimableJobs(
            @Param("statuses") Collection<ExecutionJobStatus> statuses,
            Pageable pageable
    );
}
