package com.example.back.executionjobs.controller;

import com.example.back.executionjobs.dto.ExecutionJobResponse;
import com.example.back.executionjobs.service.ExecutionJobService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/execution-jobs")
@RequiredArgsConstructor
public class ExecutionJobController {

    private final ExecutionJobService executionJobService;

    @GetMapping
    public List<ExecutionJobResponse> listJobs() {
        return executionJobService.listOwnedJobs();
    }

    @GetMapping("/{id}")
    public ExecutionJobResponse getJob(@PathVariable Long id) {
        return executionJobService.getOwnedJob(id);
    }
}
