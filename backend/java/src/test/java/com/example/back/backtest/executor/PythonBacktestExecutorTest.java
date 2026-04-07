package com.example.back.backtest.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.back.backtest.dto.BacktestRequest;
import com.example.back.backtest.dto.BacktestResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class PythonBacktestExecutorTest {

    @Test
    void parsesJsonFromStdoutEvenWithLogs() {
        String json = """
            {
              "summary": {"profit": 12.5},
              "trades": [
                {
                  "entry_time": "2024-01-01T00:00:00Z",
                  "exit_time": "2024-01-01T01:00:00Z",
                  "entry_price": 100.0,
                  "exit_price": 110.0,
                  "qty": 1.5,
                  "pnl": 10.0,
                  "fee": 0.1
                }
              ],
              "equity_curve": [
                {
                  "timestamp": "2024-01-01T00:00:00Z",
                  "cash": 10000.0,
                  "equity": 10000.0,
                  "position_size": 0.0
                }
              ],
              "logs": ["started"],
              "warnings": []
            }
            """;
        String stdout = "INFO: warmup\n" + json + "\n";
        FakeProcess process = new FakeProcess(stdout, "", 0);
        PythonBacktestExecutor executor = executorFor(process);

        BacktestRequest request = new BacktestRequest();
        request.setStrategyPath("strategy.py");
        request.setDataPath("data.csv");
        request.setStrategyParams(Map.of("length", 14));

        BacktestResult result = executor.execute(request);

        assertThat(result.getSummary()).containsEntry("profit", 12.5);
        assertThat(result.getTrades()).hasSize(1);
        assertThat(result.getTrades().get(0).getQuantity()).isEqualTo(1.5);
        assertThat(result.getTrades().get(0).getEntryTime())
            .isEqualTo(Instant.parse("2024-01-01T00:00:00Z"));
        assertThat(result.getEquityCurve()).hasSize(1);
    }

    @Test
    void throwsOnNonZeroExitCode() {
        FakeProcess process = new FakeProcess("{\"summary\":{}}", "boom", 1);
        PythonBacktestExecutor executor = executorFor(process);

        assertThatThrownBy(() -> executor.execute(minimalRequest()))
            .isInstanceOf(PythonExecutionException.class)
            .hasMessageContaining("exit code 1")
            .hasMessageContaining("boom");
    }

    @Test
    void throwsOnEmptyStdout() {
        FakeProcess process = new FakeProcess("   ", "", 0);
        PythonBacktestExecutor executor = executorFor(process);

        assertThatThrownBy(() -> executor.execute(minimalRequest()))
            .isInstanceOf(PythonExecutionException.class)
            .hasMessageContaining("empty stdout");
    }

    @Test
    void throwsOnInvalidJson() {
        FakeProcess process = new FakeProcess("not-json", "", 0);
        PythonBacktestExecutor executor = executorFor(process);

        assertThatThrownBy(() -> executor.execute(minimalRequest()))
            .isInstanceOf(PythonExecutionException.class)
            .hasMessageContaining("invalid JSON");
    }

    private PythonBacktestExecutor executorFor(FakeProcess process) {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        return new PythonBacktestExecutor(
            mapper,
            command -> process,
            "python",
            "run_backtest.py"
        );
    }

    private BacktestRequest minimalRequest() {
        BacktestRequest request = new BacktestRequest();
        request.setStrategyPath("strategy.py");
        request.setDataPath("data.csv");
        return request;
    }

    private static final class FakeProcess extends Process {
        private final InputStream inputStream;
        private final InputStream errorStream;
        private final OutputStream outputStream = new ByteArrayOutputStream();
        private final int exitCode;

        private FakeProcess(String stdout, String stderr, int exitCode) {
            this.inputStream = new ByteArrayInputStream(stdout.getBytes(StandardCharsets.UTF_8));
            this.errorStream = new ByteArrayInputStream(stderr.getBytes(StandardCharsets.UTF_8));
            this.exitCode = exitCode;
        }

        @Override
        public OutputStream getOutputStream() {
            return outputStream;
        }

        @Override
        public InputStream getInputStream() {
            return inputStream;
        }

        @Override
        public InputStream getErrorStream() {
            return errorStream;
        }

        @Override
        public int waitFor() {
            return exitCode;
        }

        @Override
        public boolean waitFor(long timeout, TimeUnit unit) {
            return true;
        }

        @Override
        public int exitValue() {
            return exitCode;
        }

        @Override
        public void destroy() {
        }

        @Override
        public boolean isAlive() {
            return false;
        }
    }
}
