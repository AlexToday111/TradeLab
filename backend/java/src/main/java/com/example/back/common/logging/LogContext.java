package com.example.back.common.logging;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.MDC;

public final class LogContext {

    public static final String REQUEST_CORRELATION_ID_ATTRIBUTE = "correlationId";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String RUN_ID_HEADER = "X-Run-Id";
    public static final String JOB_ID_HEADER = "X-Job-Id";
    public static final String MDC_CORRELATION_ID_KEY = "correlation_id";
    public static final String MDC_RUN_ID_KEY = "run_id";
    public static final String MDC_JOB_ID_KEY = "job_id";

    private LogContext() {
    }

    public static String currentCorrelationId() {
        return MDC.get(MDC_CORRELATION_ID_KEY);
    }

    public static BoundContext bind(String correlationId, String runId) {
        return bind(correlationId, runId, null);
    }

    public static BoundContext bind(String correlationId, String runId, String jobId) {
        List<MDC.MDCCloseable> closeables = new ArrayList<>();
        if (correlationId != null && !correlationId.isBlank()) {
            closeables.add(MDC.putCloseable(MDC_CORRELATION_ID_KEY, correlationId));
        }
        if (runId != null && !runId.isBlank()) {
            closeables.add(MDC.putCloseable(MDC_RUN_ID_KEY, runId));
        }
        if (jobId != null && !jobId.isBlank()) {
            closeables.add(MDC.putCloseable(MDC_JOB_ID_KEY, jobId));
        }
        return new BoundContext(closeables);
    }

    public static final class BoundContext implements AutoCloseable {
        private final List<MDC.MDCCloseable> closeables;

        private BoundContext(List<MDC.MDCCloseable> closeables) {
            this.closeables = closeables;
        }

        @Override
        public void close() {
            for (int index = closeables.size() - 1; index >= 0; index--) {
                closeables.get(index).close();
            }
        }
    }
}
