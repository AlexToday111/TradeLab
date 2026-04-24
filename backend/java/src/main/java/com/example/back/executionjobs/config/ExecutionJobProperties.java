package com.example.back.executionjobs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "execution.jobs")
public class ExecutionJobProperties {
    private boolean workerEnabled = true;
    private long pollFixedDelayMs = 1_000L;
    private int maxParallelJobs = 1;
    private int maxAttempts = 3;
    private long maxExecutionDurationMs = 0L;
    private String workerId = "";
}
