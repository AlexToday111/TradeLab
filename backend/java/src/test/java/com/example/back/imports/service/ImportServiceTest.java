package com.example.back.imports.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.back.imports.client.PythonParserClient;
import com.example.back.imports.dto.ImportCandlesRequest;
import com.example.back.imports.dto.ImportCandlesResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

    @Mock
    private PythonParserClient pythonParserClient;

    @InjectMocks
    private ImportService importService;

    @Test
    void importCandlesDelegatesToPythonClient() {
        ImportCandlesRequest request = new ImportCandlesRequest();
        request.setExchange("binance");
        request.setSymbol("BTCUSDT");
        request.setInterval("1h");
        request.setFrom("2024-01-01T00:00:00Z");
        request.setTo("2024-01-02T00:00:00Z");

        ImportCandlesResponse response = new ImportCandlesResponse();
        response.setStatus("success");
        when(pythonParserClient.importCandles(request)).thenReturn(response);

        var result = importService.importCandles(request);

        assertThat(result.getStatus()).isEqualTo("success");
        verify(pythonParserClient).importCandles(request);
    }
}
