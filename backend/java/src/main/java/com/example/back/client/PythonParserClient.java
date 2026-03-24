package com.example.back.client;

import com.example.back.config.PythonClientConfig;
import com.example.back.dto.ImportCandlesRequest;
import com.example.back.dto.ImportCandlesResponse;
import com.example.back.dto.PythonHealthResponse;
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
}
