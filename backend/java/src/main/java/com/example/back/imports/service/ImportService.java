package com.example.back.imports.service;

import com.example.back.imports.client.PythonParserClient;
import com.example.back.imports.dto.ImportCandlesRequest;
import com.example.back.imports.dto.ImportCandlesResponse;
import com.example.back.datasets.service.DatasetService;
import org.springframework.stereotype.Service;

@Service
public class ImportService {
    private final PythonParserClient pythonParserClient;
    private final DatasetService datasetService;

    public ImportService(PythonParserClient pythonParserClient, DatasetService datasetService) {
        this.pythonParserClient = pythonParserClient;
        this.datasetService = datasetService;
    }

    public ImportCandlesResponse importCandles(ImportCandlesRequest request) {
        ImportCandlesResponse response = pythonParserClient.importCandles(request);
        datasetService.upsertImportedDataset(response);
        return response;
    }
}
