package com.example.back.imports.client;

import com.example.back.common.config.PythonClientConfig;
import com.example.back.imports.dto.ImportCandlesRequest;
import com.example.back.imports.dto.ImportCandlesResponse;
import com.example.back.imports.dto.PythonHealthResponse;
import com.example.back.runs.dto.PythonRunExecuteRequest;
import com.example.back.runs.dto.PythonRunExecuteResponse;
import com.example.back.strategies.dto.StrategyValidationRequest;
import com.example.back.strategies.dto.StrategyValidationResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PythonParserClient {

    private final RestClient restClient;

    public PythonParserClient(PythonClientConfig config) {
        this.restClient = RestClient.builder()
                .baseUrl(config.getBaseUrl())
                .build();
    }

    public ImportCandlesResponse importCandles(ImportCandlesRequest request) {
        return restClient.post()
                .uri("/internal/import/candles")
                .body(request)
                .retrieve()
                .body(ImportCandlesResponse.class);
    }

    public PythonHealthResponse getHealth() {
        return restClient.get()
                .uri("/health")
                .retrieve()
                .body(PythonHealthResponse.class);
    }

    public StrategyValidationResponse validateStrategy(StrategyValidationRequest request) {
        return restClient.post()
                .uri("/internal/strategies/validate")
                .body(request)
                .retrieve()
                .body(StrategyValidationResponse.class);
    }

    public PythonRunExecuteResponse executeRun(PythonRunExecuteRequest request) {
        return restClient.post()
                .uri("/internal/runs/execute")
                .body(request)
                .retrieve()
                .body(PythonRunExecuteResponse.class);
    }
}
