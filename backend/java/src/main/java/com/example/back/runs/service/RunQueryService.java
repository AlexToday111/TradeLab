package com.example.back.runs.service;

import com.example.back.runs.dto.RunResponse;
import com.example.back.runs.entity.RunEntity;
import com.example.back.runs.mapper.RunMapper;
import com.example.back.runs.repository.RunRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RunQueryService {

    private static final Sort RUNS_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final RunRepository runRepository;
    private final RunMapper runMapper;

    public List<RunResponse> listRecentRuns(int limit) {
        return runRepository.findAll(PageRequest.of(0, limit, RUNS_SORT)).stream()
                .map(runMapper::toResponse)
                .toList();
    }

    public Optional<RunResponse> findRun(Long runId) {
        return runRepository.findById(runId)
                .map(runMapper::toResponse);
    }

    public Optional<RunResponse> findLastRun() {
        return runRepository.findAll(PageRequest.of(0, 1, RUNS_SORT)).stream()
                .findFirst()
                .map(this::toResponse);
    }

    private RunResponse toResponse(RunEntity runEntity) {
        return runMapper.toResponse(runEntity);
    }
}
