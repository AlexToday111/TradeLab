package com.example.back.controller;


import com.example.back.client.PythonParserClient;
import com.example.back.dto.PythonHealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    private final PythonParserClient pythonClient;

    public HealthController(PythonParserClient pythonClient) {
        this.pythonClient = pythonClient;
    }

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "ok",
                "service", "java-api"
        );
    }

    @GetMapping("/api/python/health")
    public PythonHealthResponse pythonHealth() {
        return pythonClient.getHealth();
    }

}
