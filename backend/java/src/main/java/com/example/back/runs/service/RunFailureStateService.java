package com.example.back.runs.service;

import com.example.back.backtest.exception.BacktestResourceNotFoundException;
import com.example.back.backtest.model.BacktestStatus;
import com.example.back.common.logging.LogContext;
import com.example.back.runs.entity.RunEntity;
import com.example.back.runs.repository.RunRepository;
import com.example.back.telegram.service.TelegramNotificationService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class RunFailureStateService {

    private final RunRepository runRepository;
    private final RunQueryService runQueryService;
    private final TelegramNotificationService telegramNotificationService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailedInNewTransaction(Long runId, String message) {
        markFailedInNewTransaction(runId, message, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailedInNewTransaction(Long runId, String message, String errorDetailsJson) {
        RunEntity run = runRepository.findById(runId)
                .orElseThrow(() -> new BacktestResourceNotFoundException("Run not found: " + runId));

        try (LogContext.BoundContext ignored = LogContext.bind(run.getCorrelationId(), String.valueOf(run.getId()))) {
            BacktestStatus previousStatus = run.getStatus();
            run.setStatus(BacktestStatus.FAILED);
            run.setErrorMessage(normalizeError(message));
            run.setErrorDetailsJson(errorDetailsJson);
            run.setFinishedAt(Instant.now());
            run.setExecutionDurationMs(resolveExecutionDurationMs(run.getStartedAt(), run.getFinishedAt()));
            runRepository.save(run);
            log.warn("Run status transition {} -> FAILED", previousStatus);
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendRunFailedNotification(runId);
                }
            });
        }
    }

    private String normalizeError(String message) {
        if (message == null || message.isBlank()) {
            return "Backtest execution failed";
        }
        return message;
    }

    private Long resolveExecutionDurationMs(Instant startedAt, Instant finishedAt) {
        if (startedAt == null || finishedAt == null) {
            return null;
        }
        return finishedAt.toEpochMilli() - startedAt.toEpochMilli();
    }

    private void sendRunFailedNotification(Long runId) {
        try {
            runQueryService.findRun(runId)
                    .ifPresent(telegramNotificationService::sendRunFailed);
        } catch (RuntimeException ex) {
            log.warn("Failed to dispatch Telegram failure notification for run {}", runId, ex);
        }
    }
}
