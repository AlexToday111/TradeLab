package com.example.back.runs.service;

import com.example.back.backtest.model.BacktestStatus;
import com.example.back.runs.repository.RunRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.example.back.telegram.service.TelegramNotificationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class RunFailureStateService {

    private final RunRepository runRepository;
    private final RunQueryService runQueryService;
    private final TelegramNotificationService telegramNotificationService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailedInNewTransaction(Long runId, String message) {
        int updatedRows = runRepository.updateFailureState(
                runId,
                BacktestStatus.FAILED,
                normalizeError(message),
                Instant.now()
        );
        if (updatedRows > 0 && TransactionSynchronizationManager.isSynchronizationActive()) {
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

    private void sendRunFailedNotification(Long runId) {
        try {
            runQueryService.findRun(runId)
                    .ifPresent(telegramNotificationService::sendRunFailed);
        } catch (RuntimeException ex) {
            log.warn("Failed to dispatch Telegram failure notification for run {}", runId, ex);
        }
    }
}
