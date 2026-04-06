package com.example.back.imports.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.back.imports.dto.ImportCandlesResponse;
import com.example.back.imports.service.ImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ImportControllerTest {

    @Mock
    private ImportService importService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ImportController(importService)).build();
    }

    @Test
    void importCandlesReturnsServiceResponse() throws Exception {
        ImportCandlesResponse response = new ImportCandlesResponse();
        response.setStatus("success");
        response.setImported(24);
        when(importService.importCandles(org.mockito.ArgumentMatchers.any())).thenReturn(response);

        mockMvc.perform(
                post("/api/imports/candles")
                    .contentType("application/json")
                    .content(
                        """
                        {
                          "exchange":"binance",
                          "symbol":"BTCUSDT",
                          "interval":"1h",
                          "from":"2024-01-01T00:00:00Z",
                          "to":"2024-01-02T00:00:00Z"
                        }
                        """
                    )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("success")))
            .andExpect(jsonPath("$.imported", is(24)));
    }
}
