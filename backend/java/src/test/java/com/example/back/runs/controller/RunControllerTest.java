package com.example.back.runs.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.back.runs.dto.RunResponse;
import com.example.back.runs.service.RunService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class RunControllerTest {

    @Mock
    private RunService runService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RunController(runService)).build();
    }

    @Test
    void createRunReturnsCreatedResponse() throws Exception {
        when(runService.createRun(org.mockito.ArgumentMatchers.any())).thenReturn(runResponse(1L));

        mockMvc.perform(
                post("/api/runs")
                    .contentType("application/json")
                    .content(
                        """
                        {
                          "strategyId":1,
                          "exchange":"binance",
                          "symbol":"BTCUSDT",
                          "interval":"1h",
                          "from":"2024-01-01T00:00:00Z",
                          "to":"2024-01-02T00:00:00Z",
                          "params":{"length":20}
                        }
                        """
                    )
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    void getRunsReturnsList() throws Exception {
        when(runService.getRuns()).thenReturn(List.of(runResponse(1L)));

        mockMvc.perform(get("/api/runs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].symbol", is("BTCUSDT")));
    }

    @Test
    void getRunByIdReturnsSingleRun() throws Exception {
        when(runService.getRunById(1L)).thenReturn(runResponse(1L));

        mockMvc.perform(get("/api/runs/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)));
    }

    private RunResponse runResponse(Long id) {
        return new RunResponse(
            id,
            1L,
            "COMPLETED",
            "binance",
            "BTCUSDT",
            "1h",
            "2024-01-01T00:00:00Z",
            "2024-01-02T00:00:00Z",
            Map.of("length", 20),
            Map.of("profit", 10),
            null,
            Instant.parse("2024-01-01T00:00:00Z"),
            Instant.parse("2024-01-01T00:10:00Z")
        );
    }
}
