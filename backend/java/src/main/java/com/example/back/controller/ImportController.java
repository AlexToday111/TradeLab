package com.example.back.controller;

import com.example.back.dto.ImportCandlesRequest;
import com.example.back.dto.ImportCandlesResponse;
import com.example.back.service.ImportService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/imports")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/candles")
    public ImportCandlesResponse importCandles(@RequestBody ImportCandlesRequest request) {
        return importService.importCandles(request);
    }
}