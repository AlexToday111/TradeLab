package com.example.back.controller;

import com.example.back.imports.client.PythonParserClient;
import com.example.back.imports.dto.PythonHealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(
        name = "Health",
        description = "Проверка состояния сервисов системы"
)
public class HealthController {

    private final PythonParserClient pythonClient;

    public HealthController(PythonParserClient pythonClient) {
        this.pythonClient = pythonClient;
    }

    @Operation(
            summary = "Проверка Java API",
            description = "Возвращает статус текущего Java сервиса"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Сервис работает"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "ok",
                "service", "java-api"
        );
    }

    @Operation(
            summary = "Проверка Python сервиса",
            description = "Проксирует health-check Python parser сервиса"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Python сервис доступен"),
            @ApiResponse(responseCode = "503", description = "Python сервис недоступен")
    })
    @GetMapping("/api/python/health")
    public PythonHealthResponse pythonHealth() {
        return pythonClient.getHealth();
    }
}