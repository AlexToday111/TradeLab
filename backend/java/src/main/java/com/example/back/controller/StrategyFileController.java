package com.example.back.controller;

import com.example.back.client.PythonParserClient;
import com.example.back.dto.StrategyResponse;
import com.example.back.service.StrategyFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/strategies")
@RequiredArgsConstructor
public class StrategyFileController {

    private final StrategyFileService strategyFileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StrategyResponse> uploadStrategy(
            @RequestParam("file") MultipartFile file) {
        StrategyResponse response = strategyFileService.uploadStrategy(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<StrategyResponse>> getAllStrategies() {
        List<StrategyResponse> strategies = strategyFileService.getAllStrategies();
        return ResponseEntity.ok(strategies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StrategyResponse> getStrategyById(@PathVariable Long id) {
        StrategyResponse strategy = strategyFileService.getStrategyById(id);
        return ResponseEntity.ok(strategy);
    }
}