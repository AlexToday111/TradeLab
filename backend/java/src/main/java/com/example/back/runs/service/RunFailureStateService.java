package com.example.back.runs.service;

import com.example.back.backtest.model.BacktestStatus;
import com.example.back.runs.repository.RunRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RunFailureStateService {

    private final RunRepository runRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailedInNewTransaction(Long runId, String message) {
        runRepository.updateFailureState(
                runId,
                BacktestStatus.FAILED,
                normalizeError(message),
                Instant.now()
        );
    }

    private String normalizeError(String message) {
        if (message == null || message.isBlank()) {
            return "Backtest execution failed";
        }
        return message;
    }
}
