package com.example.back.runs.controller;

import com.example.back.backtest.dto.CreateBacktestRunRequest;
import com.example.back.runs.dto.RunResultResponse;
import com.example.back.runs.dto.RunResponse;
import com.example.back.runs.service.RunService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/runs")
@RequiredArgsConstructor
public class RunController {

    private final RunService runService;

    @GetMapping
    public List<RunResponse> listRuns() {
        return runService.listRuns();
    }

    @GetMapping("/{id}")
    public RunResponse getRun(@PathVariable Long id) {
        return runService.getRun(id);
    }

    @GetMapping("/{id}/result")
    public RunResultResponse getRunResult(@PathVariable Long id) {
        return runService.getRunResult(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RunResponse createRun(@Valid @RequestBody CreateBacktestRunRequest request) {
        return runService.createRun(request);
    }

    @PostMapping("/{id}/rerun")
    @ResponseStatus(HttpStatus.CREATED)
    public RunResponse rerun(@PathVariable Long id) {
        return runService.rerun(id);
    }
}
