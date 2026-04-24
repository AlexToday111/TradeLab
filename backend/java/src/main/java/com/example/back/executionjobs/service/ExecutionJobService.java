package com.example.back.executionjobs.service;

import static net.logstash.logback.argument.StructuredArguments.entries;

import com.example.back.auth.security.AuthContext;
import com.example.back.backtest.exception.BacktestResourceNotFoundException;
import com.example.back.backtest.exception.BacktestValidationException;
import com.example.back.backtest.model.BacktestStatus;
import com.example.back.executionjobs.config.ExecutionJobProperties;
import com.example.back.executionjobs.dto.ExecutionJobResponse;
import com.example.back.executionjobs.entity.ExecutionJobEntity;
import com.example.back.executionjobs.entity.ExecutionJobStatus;
import com.example.back.executionjobs.repository.ExecutionJobRepository;
import com.example.back.runs.entity.RunEntity;
import com.example.back.runs.repository.RunRepository;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionJobService {

    private static final Collection<ExecutionJobStatus> ACTIVE_STATUSES = List.of(
            ExecutionJobStatus.QUEUED,
            ExecutionJobStatus.RETRYING,
            ExecutionJobStatus.RUNNING
    );

    private static final Collection<ExecutionJobStatus> CLAIMABLE_STATUSES = List.of(
            ExecutionJobStatus.QUEUED,
            ExecutionJobStatus.RETRYING
    );

    private final ExecutionJobRepository executionJobRepository;
    private final RunRepository runRepository;
    private final ExecutionJobProperties properties;

    @Transactional(readOnly = true)
    public List<ExecutionJobResponse> listOwnedJobs() {
        Long userId = AuthContext.requireUserId();
        return executionJobRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExecutionJobResponse getOwnedJob(Long jobId) {
        Long userId = AuthContext.requireUserId();
        return executionJobRepository.findByIdAndUserId(jobId, userId)
                .map(this::toResponse)
                .orElseThrow(() -> new BacktestResourceNotFoundException("Execution job not found: " + jobId));
    }

    @Transactional(readOnly = true)
    public ExecutionJobResponse getLatestOwnedRunJob(Long runId) {
        Long userId = AuthContext.requireUserId();
        requireOwnedRun(runId, userId);
        return executionJobRepository.findFirstByRunIdAndUserIdOrderByCreatedAtDesc(runId, userId)
                .map(this::toResponse)
                .orElseThrow(() -> new BacktestResourceNotFoundException("Execution job not found for run: " + runId));
    }

    @Transactional
    public ExecutionJobEntity createQueuedJob(RunEntity run) {
        if (executionJobRepository.existsByRunIdAndStatusIn(run.getId(), ACTIVE_STATUSES)) {
            throw new BacktestValidationException("Run already has an active execution job");
        }

        ExecutionJobEntity job = new ExecutionJobEntity();
        job.setRunId(run.getId());
        job.setUserId(run.getUserId());
        job.setStatus(ExecutionJobStatus.QUEUED);
        job.setPriority(0);
        job.setAttemptCount(0);
        job.setMaxAttempts(properties.getMaxAttempts());
        job.setQueuedAt(Instant.now());
        job.setCancelRequested(false);
        ExecutionJobEntity saved = executionJobRepository.saveAndFlush(job);
        log.info("Execution job created {}", entries(jobLogPayload(saved, "execution_job_created")));
        return saved;
    }

    @Transactional
    public Optional<ExecutionJobEntity> claimNextJob(String workerId) {
        List<ExecutionJobEntity> jobs = executionJobRepository.findClaimableJobs(
                CLAIMABLE_STATUSES,
                PageRequest.of(0, 1)
        );
        if (jobs.isEmpty()) {
            return Optional.empty();
        }

        ExecutionJobEntity job = jobs.get(0);
        if (Boolean.TRUE.equals(job.getCancelRequested())) {
            markCanceled(job.getId(), "Job was canceled before claim");
            return Optional.empty();
        }

        ExecutionJobStatus previousStatus = job.getStatus();
        Instant now = Instant.now();
        job.setStatus(ExecutionJobStatus.RUNNING);
        job.setLockedAt(now);
        job.setLockedBy(workerId);
        job.setStartedAt(now);
        job.setFinishedAt(null);
        job.setAttemptCount(safeInteger(job.getAttemptCount()) + 1);
        ExecutionJobEntity saved = executionJobRepository.saveAndFlush(job);
        log.info("Execution job claimed {}", entries(jobTransitionPayload(saved, previousStatus)));
        return Optional.of(saved);
    }

    @Transactional
    public void markSucceeded(Long jobId) {
        ExecutionJobEntity job = findJob(jobId);
        ExecutionJobStatus previousStatus = job.getStatus();
        job.setStatus(ExecutionJobStatus.SUCCEEDED);
        job.setFinishedAt(Instant.now());
        job.setLockedAt(null);
        job.setLockedBy(null);
        job.setCancelRequested(false);
        job.setErrorCode(null);
        job.setErrorMessage(null);
        executionJobRepository.save(job);
        log.info("Execution job succeeded {}", entries(jobTransitionPayload(job, previousStatus)));
    }

    @Transactional
    public void markFailed(Long jobId, String errorCode, String errorMessage) {
        ExecutionJobEntity job = findJob(jobId);
        ExecutionJobStatus previousStatus = job.getStatus();
        job.setStatus(ExecutionJobStatus.FAILED);
        job.setFinishedAt(Instant.now());
        job.setLockedAt(null);
        job.setLockedBy(null);
        job.setErrorCode(normalizeErrorCode(errorCode));
        job.setErrorMessage(normalizeErrorMessage(errorMessage));
        executionJobRepository.save(job);
        log.warn("Execution job failed {}", entries(jobTransitionPayload(job, previousStatus)));
    }

    @Transactional
    public void markCanceled(Long jobId, String message) {
        ExecutionJobEntity job = findJob(jobId);
        ExecutionJobStatus previousStatus = job.getStatus();
        job.setStatus(ExecutionJobStatus.CANCELED);
        job.setFinishedAt(Instant.now());
        job.setLockedAt(null);
        job.setLockedBy(null);
        job.setCancelRequested(true);
        job.setErrorCode("CANCELED");
        job.setErrorMessage(message);
        executionJobRepository.save(job);
        markRunCanceled(job.getRunId());
        log.info("Execution job canceled {}", entries(jobTransitionPayload(job, previousStatus)));
    }

    @Transactional(readOnly = true)
    public boolean isCancelRequested(Long jobId) {
        return Boolean.TRUE.equals(findJob(jobId).getCancelRequested());
    }

    @Transactional
    public ExecutionJobResponse retryRun(Long runId) {
        Long userId = AuthContext.requireUserId();
        RunEntity run = requireOwnedRun(runId, userId);
        ExecutionJobEntity job = executionJobRepository
                .findFirstByRunIdAndUserIdOrderByCreatedAtDesc(runId, userId)
                .orElseThrow(() -> new BacktestResourceNotFoundException("Execution job not found for run: " + runId));

        if (job.getStatus() == ExecutionJobStatus.CANCELED) {
            throw new BacktestValidationException("Canceled execution jobs cannot be retried");
        }
        if (ACTIVE_STATUSES.contains(job.getStatus())) {
            throw new BacktestValidationException("Execution job is already active");
        }
        if (safeInteger(job.getAttemptCount()) >= safeInteger(job.getMaxAttempts())) {
            throw new BacktestValidationException("Execution job retry limit reached");
        }

        ExecutionJobStatus previousStatus = job.getStatus();
        job.setStatus(ExecutionJobStatus.RETRYING);
        job.setQueuedAt(Instant.now());
        job.setStartedAt(null);
        job.setFinishedAt(null);
        job.setLockedAt(null);
        job.setLockedBy(null);
        job.setCancelRequested(false);
        executionJobRepository.saveAndFlush(job);

        job.setStatus(ExecutionJobStatus.QUEUED);
        executionJobRepository.save(job);

        run.setStatus(BacktestStatus.QUEUED);
        run.setStartedAt(null);
        run.setFinishedAt(null);
        runRepository.save(run);
        log.info("Execution job retried {}", entries(jobTransitionPayload(job, previousStatus)));
        return toResponse(job);
    }

    @Transactional
    public ExecutionJobResponse cancelRun(Long runId) {
        Long userId = AuthContext.requireUserId();
        RunEntity run = requireOwnedRun(runId, userId);
        ExecutionJobEntity job = executionJobRepository
                .findFirstByRunIdAndUserIdOrderByCreatedAtDesc(runId, userId)
                .orElseThrow(() -> new BacktestResourceNotFoundException("Execution job not found for run: " + runId));

        if (job.getStatus() == ExecutionJobStatus.SUCCEEDED
                || job.getStatus() == ExecutionJobStatus.FAILED
                || job.getStatus() == ExecutionJobStatus.CANCELED) {
            throw new BacktestValidationException("Execution job is already finished");
        }

        if (job.getStatus() == ExecutionJobStatus.RUNNING) {
            job.setCancelRequested(true);
            job.setErrorCode("CANCEL_REQUESTED");
            job.setErrorMessage("Cancellation requested while job is running");
            run.setStatus(BacktestStatus.CANCELED);
            run.setFinishedAt(Instant.now());
            run.setExecutionDurationMs(resolveExecutionDurationMs(run.getStartedAt(), run.getFinishedAt()));
            runRepository.save(run);
            executionJobRepository.save(job);
            log.info("Execution job cancellation requested {}", entries(jobLogPayload(job, "execution_job_cancel_requested")));
            return toResponse(job);
        }

        job.setCancelRequested(true);
        job.setStatus(ExecutionJobStatus.CANCELED);
        job.setFinishedAt(Instant.now());
        job.setErrorCode("CANCELED");
        job.setErrorMessage("Job canceled before execution");
        executionJobRepository.save(job);
        markRunCanceled(run);
        log.info("Execution job canceled before execution {}", entries(jobLogPayload(job, "execution_job_canceled")));
        return toResponse(job);
    }

    @Transactional(readOnly = true)
    public ExecutionJobEntity findJob(Long jobId) {
        return executionJobRepository.findById(jobId)
                .orElseThrow(() -> new BacktestResourceNotFoundException("Execution job not found: " + jobId));
    }

    public ExecutionJobResponse toResponse(ExecutionJobEntity job) {
        return ExecutionJobResponse.builder()
                .id(job.getId())
                .runId(job.getRunId())
                .userId(job.getUserId())
                .status(job.getStatus())
                .priority(job.getPriority())
                .attemptCount(job.getAttemptCount())
                .maxAttempts(job.getMaxAttempts())
                .queuedAt(job.getQueuedAt())
                .startedAt(job.getStartedAt())
                .finishedAt(job.getFinishedAt())
                .lockedAt(job.getLockedAt())
                .lockedBy(job.getLockedBy())
                .cancelRequested(job.getCancelRequested())
                .errorCode(job.getErrorCode())
                .errorMessage(job.getErrorMessage())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    private RunEntity requireOwnedRun(Long runId, Long userId) {
        return runRepository.findByIdAndUserId(runId, userId)
                .orElseThrow(() -> new BacktestResourceNotFoundException("Run not found: " + runId));
    }

    private void markRunCanceled(Long runId) {
        RunEntity run = runRepository.findById(runId)
                .orElseThrow(() -> new BacktestResourceNotFoundException("Run not found: " + runId));
        markRunCanceled(run);
    }

    private void markRunCanceled(RunEntity run) {
        run.setStatus(BacktestStatus.CANCELED);
        run.setFinishedAt(Instant.now());
        run.setExecutionDurationMs(resolveExecutionDurationMs(run.getStartedAt(), run.getFinishedAt()));
        runRepository.save(run);
    }

    private Map<String, Object> jobTransitionPayload(ExecutionJobEntity job, ExecutionJobStatus previousStatus) {
        Map<String, Object> payload = jobLogPayload(job, "execution_job_status_transition");
        payload.put("previous_status", previousStatus);
        payload.put("next_status", job.getStatus());
        return payload;
    }

    private Map<String, Object> jobLogPayload(ExecutionJobEntity job, String event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", event);
        payload.put("job_id", job.getId());
        payload.put("run_id", job.getRunId());
        payload.put("user_id", job.getUserId());
        payload.put("status", job.getStatus());
        payload.put("attempt_count", job.getAttemptCount());
        payload.put("max_attempts", job.getMaxAttempts());
        payload.put("locked_by", job.getLockedBy());
        payload.put("locked_at", job.getLockedAt());
        payload.put("cancel_requested", job.getCancelRequested());
        payload.put("error_code", job.getErrorCode());
        payload.put("error_message", job.getErrorMessage());
        return payload;
    }

    private String normalizeErrorCode(String errorCode) {
        if (errorCode == null || errorCode.isBlank()) {
            return "EXECUTION_FAILED";
        }
        return errorCode;
    }

    private String normalizeErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return "Execution job failed";
        }
        return errorMessage;
    }

    private int safeInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private Long resolveExecutionDurationMs(Instant startedAt, Instant finishedAt) {
        if (startedAt == null || finishedAt == null) {
            return null;
        }
        return finishedAt.toEpochMilli() - startedAt.toEpochMilli();
    }
}
