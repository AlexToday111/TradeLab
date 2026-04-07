package com.example.back.backtest.executor;

import com.example.back.backtest.dto.BacktestRequest;
import com.example.back.backtest.dto.BacktestResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class PythonBacktestExecutor {

    private final ObjectMapper objectMapper;

    private final String pythonExecutable = "python";
    private final String scriptPath = "python/backtesting/run_backtest.py";

    public BacktestResult execute(BacktestRequest request) {
        try {
            String inputJson = objectMapper.writeValueAsString(request);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    pythonExecutable,
                    scriptPath
            );

            Process process = processBuilder.start();

            writeToStdin(process, inputJson);

            String stdout = readStream(process.getInputStream());
            String stderr = readStream(process.getErrorStream());

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new PythonExecutionException(
                        "Python process failed with exit code " + exitCode + ". stderr: " + stderr
                );
            }

            if (stdout == null || stdout.isBlank()) {
                throw new PythonExecutionException("Python process returned empty stdout");
            }

            return objectMapper.readValue(stdout, BacktestResult.class);

        } catch (PythonExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new PythonExecutionException("Failed to execute Python backtest", e);
        }
    }

    private void writeToStdin(Process process, String inputJson) throws IOException {
        try (OutputStream os = process.getOutputStream()) {
            os.write(inputJson.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
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
}