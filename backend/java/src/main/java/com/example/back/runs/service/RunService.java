package com.example.back.runs.service;

import com.example.back.backtest.dto.CreateBacktestRunRequest;
import com.example.back.backtest.exception.BacktestResourceNotFoundException;
import com.example.back.backtest.service.BacktestService;
import com.example.back.runs.dto.RunResponse;
import com.example.back.runs.entity.RunEntity;
import com.example.back.runs.mapper.RunMapper;
import com.example.back.runs.repository.RunRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RunService {

    private static final Sort RUNS_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final RunRepository runRepository;
    private final RunMapper runMapper;
    private final BacktestService backtestService;

    public List<RunResponse> listRuns() {
        return runRepository.findAll(RUNS_SORT).stream()
                .map(runMapper::toResponse)
                .toList();
    }

    public RunResponse getRun(Long runId) {
        return runMapper.toResponse(findRunEntity(runId));
    }

    public RunResponse createRun(CreateBacktestRunRequest request) {
        Long runId = backtestService.createRun(request);

        try {
            backtestService.executeRun(runId);
        } catch (RuntimeException ex) {
            return getRun(runId);
        }

        return getRun(runId);
    }

    public RunResponse rerun(Long runId) {
        Long rerunId = backtestService.rerun(runId);

        try {
            backtestService.executeRun(rerunId);
        } catch (RuntimeException ex) {
            return getRun(rerunId);
        }

        return getRun(rerunId);
    }

    private RunEntity findRunEntity(Long runId) {
        return runRepository.findById(runId)
                .orElseThrow(() -> new BacktestResourceNotFoundException("Run not found: " + runId));
    }
}
