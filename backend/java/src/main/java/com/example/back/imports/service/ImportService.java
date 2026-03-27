package com.example.back.imports.service;

import com.example.back.imports.client.PythonParserClient;
import com.example.back.imports.dto.ImportCandlesRequest;
import com.example.back.imports.dto.ImportCandlesResponse;
import org.springframework.stereotype.Service;

@Service
public class ImportService {
    private final PythonParserClient pythonParserClient;

    public ImportService(PythonParserClient pythonParserClient) {
        this.pythonParserClient = pythonParserClient;
    }

    public ImportCandlesResponse importCandles(ImportCandlesRequest request) {
        return pythonParserClient.importCandles(request);
    }
}
