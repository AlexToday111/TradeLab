package com.example.back.backtest.executor;

import com.example.back.backtest.dto.BacktestRequest;
import com.example.back.backtest.dto.BacktestResult;
import com.example.back.backtest.dto.BacktestTrade;
import com.example.back.backtest.dto.EquityPoint;
import com.example.back.backtest.dto.PythonBacktestArtifacts;
import com.example.back.backtest.dto.PythonBacktestResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PythonBacktestExecutor {

    private static final TypeReference<List<BacktestTrade>> TRADE_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<EquityPoint>> EQUITY_LIST_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final ProcessLauncher processLauncher;
    private final String pythonExecutable;
    private final String scriptPath;

    public PythonBacktestExecutor(
            ObjectMapper objectMapper,
            ProcessLauncher processLauncher,
            @Value("${python.backtest.executable:python}") String pythonExecutable,
            @Value("${python.backtest.script:../python/backtesting/run_backtest.py}") String scriptPath
    ) {
        this.objectMapper = objectMapper;
        this.processLauncher = processLauncher;
        this.pythonExecutable = pythonExecutable;
        this.scriptPath = scriptPath;
    }

    public BacktestResult execute(BacktestRequest request) {
        String inputJson = serializeRequest(request);
        List<String> command = List.of(pythonExecutable, scriptPath);

        try {
            Process process = processLauncher.start(command);
            writeToStdin(process, inputJson);

            CompletableFuture<String> stdoutFuture = readStreamAsync(process.getInputStream());
            CompletableFuture<String> stderrFuture = readStreamAsync(process.getErrorStream());

            int exitCode = process.waitFor();

            String stdout = getFuture(stdoutFuture, "stdout");
            String stderr = getFuture(stderrFuture, "stderr");

            if (stdout == null || stdout.isBlank()) {
                throw new PythonExecutionException("Python process returned empty stdout");
            }

            PythonBacktestResponse response = parseResponse(stdout, stderr);

            if (exitCode != 0 || !isSuccess(response)) {
                throw new PythonExecutionException(buildFailureMessage(response, exitCode, stderr));
            }

            return toBacktestResult(response);

        } catch (PythonExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new PythonExecutionException("Failed to execute Python backtest", e);
        }
    }

    private String serializeRequest(BacktestRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new PythonExecutionException("Failed to serialize backtest request", e);
        }
    }

    private PythonBacktestResponse parseResponse(String stdout, String stderr) {
        String trimmed = stdout.trim();
        try {
            return objectMapper.readValue(trimmed, PythonBacktestResponse.class);
        } catch (Exception e) {
            String candidate = extractJsonCandidate(trimmed);
            if (candidate != null) {
                try {
                    return objectMapper.readValue(candidate, PythonBacktestResponse.class);
                } catch (Exception ignored) {
                    log.debug("Failed to parse JSON candidate from stdout: {}", candidate);
                }
            }
            throw new PythonExecutionException(
                    "Python process returned invalid JSON. stdout: " + trimmed + ". stderr: " + stderr,
                    e
            );
        }
    }

    private boolean isSuccess(PythonBacktestResponse response) {
        return response != null && "SUCCESS".equalsIgnoreCase(response.getStatus());
    }

    private String buildFailureMessage(PythonBacktestResponse response, int exitCode, String stderr) {
        StringBuilder message = new StringBuilder("Python backtest failed");
        if (exitCode != 0) {
            message.append(" with exit code ").append(exitCode);
        }
        if (response != null && response.getErrorMessage() != null && !response.getErrorMessage().isBlank()) {
            message.append(". errorMessage: ").append(response.getErrorMessage());
        }
        if (stderr != null && !stderr.isBlank()) {
            message.append(". stderr: ").append(stderr);
        }
        return message.toString();
    }

    private BacktestResult toBacktestResult(PythonBacktestResponse response) {
        BacktestResult result = new BacktestResult();
        result.setSummary(response.getMetrics());

        PythonBacktestArtifacts artifacts = response.getArtifacts();
        if (artifacts != null) {
            result.setTrades(readArtifactList(artifacts.getTradesPath(), TRADE_LIST_TYPE));
            result.setEquityCurve(readArtifactList(artifacts.getEquityCurvePath(), EQUITY_LIST_TYPE));
        } else {
            result.setTrades(List.of());
            result.setEquityCurve(List.of());
        }

        result.setLogs(List.of());
        result.setWarnings(List.of());
        return result;
    }

    private <T> List<T> readArtifactList(String path, TypeReference<List<T>> typeReference) {
        if (path == null || path.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(Files.readString(Path.of(path), StandardCharsets.UTF_8), typeReference);
        } catch (Exception e) {
            throw new PythonExecutionException("Failed to read Python artifact file: " + path, e);
        }
    }

    private String extractJsonCandidate(String stdout) {
        List<String> lines = stdout.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
        for (int i = lines.size() - 1; i >= 0; i--) {
            String line = lines.get(i);
            if (line.startsWith("{") && line.endsWith("}")) {
                return line;
            }
        }

        int start = stdout.indexOf('{');
        int end = stdout.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return stdout.substring(start, end + 1);
        }
        return null;
    }

    private void writeToStdin(Process process, String inputJson) throws IOException {
        try (OutputStream os = process.getOutputStream()) {
            os.write(inputJson.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
    }

    private CompletableFuture<String> readStreamAsync(InputStream inputStream) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return readStream(inputStream);
            } catch (IOException e) {
                throw new PythonExecutionException("Failed to read process stream", e);
            }
        });
    }

    private String readStream(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            return sb.toString().trim();
        }
    }

    private String getFuture(CompletableFuture<String> future, String label)
            throws ExecutionException, InterruptedException {
        try {
            return future.get();
        } catch (ExecutionException e) {
            throw new PythonExecutionException("Failed to read process " + label, e.getCause());
        }
    }
}
