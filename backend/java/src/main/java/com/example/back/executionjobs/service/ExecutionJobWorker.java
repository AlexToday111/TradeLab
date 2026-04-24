package com.example.back.executionjobs.service;

import com.example.back.executionjobs.config.ExecutionJobProperties;
import com.example.back.executionjobs.entity.ExecutionJobEntity;
import com.example.back.runs.service.RunOrchestrationService;
import jakarta.annotation.PreDestroy;
import java.lang.management.ManagementFactory;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExecutionJobWorker {

    private final ExecutionJobService executionJobService;
    private final RunOrchestrationService runOrchestrationService;
    private final ExecutionJobProperties properties;
    private final AtomicInteger activeJobs = new AtomicInteger(0);
    private final ExecutorService executorService;
    private final String workerId;

    public ExecutionJobWorker(
            ExecutionJobService executionJobService,
            RunOrchestrationService runOrchestrationService,
            ExecutionJobProperties properties
    ) {
        this.executionJobService = executionJobService;
        this.runOrchestrationService = runOrchestrationService;
        this.properties = properties;
        this.workerId = resolveWorkerId(properties.getWorkerId());
        this.executorService = Executors.newFixedThreadPool(
                Math.max(1, properties.getMaxParallelJobs()),
                threadFactory()
        );
    }

    @Scheduled(fixedDelayString = "${execution.jobs.poll-fixed-delay-ms:1000}")
    public void poll() {
        if (!properties.isWorkerEnabled()) {
            return;
        }
        dispatchAvailableCapacity();
    }

    public boolean processOneJobSync() {
        Optional<ExecutionJobEntity> claimed = executionJobService.claimNextJob(workerId);
        claimed.ifPresent(job -> runOrchestrationService.executeJob(job.getId()));
        return claimed.isPresent();
    }

    private void dispatchAvailableCapacity() {
        while (activeJobs.get() < Math.max(1, properties.getMaxParallelJobs())) {
            Optional<ExecutionJobEntity> claimed = executionJobService.claimNextJob(workerId);
            if (claimed.isEmpty()) {
                return;
            }
            activeJobs.incrementAndGet();
            Long jobId = claimed.orElseThrow().getId();
            executorService.execute(() -> {
                try {
                    runOrchestrationService.executeJob(jobId);
                } catch (RuntimeException exception) {
                    log.error("Execution worker failed to process job {}", jobId, exception);
                } finally {
                    activeJobs.decrementAndGet();
                }
            });
        }
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
    }

    private ThreadFactory threadFactory() {
        AtomicInteger threadIndex = new AtomicInteger(0);
        return runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(workerId + "-" + threadIndex.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }

    private String resolveWorkerId(String configuredWorkerId) {
        if (configuredWorkerId != null && !configuredWorkerId.isBlank()) {
            return configuredWorkerId;
        }
        return "java-worker-" + ManagementFactory.getRuntimeMXBean().getName().replace('@', '-');
    }
}
