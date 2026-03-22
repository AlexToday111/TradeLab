package com.example.back.service;

import com.example.back.client.PythonParserClient;
import com.example.back.dto.ImportCandlesRequest;
import com.example.back.dto.ImportCandlesResponse;
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
