package com.example.back.runs.controller;

import com.example.back.runs.dto.CreateRunRequest;
import com.example.back.runs.dto.RunResponse;
import com.example.back.runs.service.RunService;
import jakarta.validation.Valid;
import java.util.List;
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
public class RunController {
    private final RunService runService;

    public RunController(RunService runService) {
        this.runService = runService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RunResponse createRun(@Valid @RequestBody CreateRunRequest request) {
        return runService.createRun(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<RunResponse> getRuns() {
        return runService.getRuns();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RunResponse getRunById(@PathVariable Long id) {
        return runService.getRunById(id);
    }
}
