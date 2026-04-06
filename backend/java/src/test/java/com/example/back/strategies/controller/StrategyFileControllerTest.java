package com.example.back.strategies.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.back.strategies.dto.StrategyResponse;
import com.example.back.strategies.entity.StrategyFileEntity;
import com.example.back.strategies.service.StrategyFileService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class StrategyFileControllerTest {

    @Mock
    private StrategyFileService strategyFileService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new StrategyFileController(strategyFileService)).build();
    }

    @Test
    void uploadStrategyReturnsCreatedResponse() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "ema.py", "text/x-python", "x".getBytes());
        when(strategyFileService.uploadStrategy(org.mockito.ArgumentMatchers.any())).thenReturn(strategyResponse(1L));

        mockMvc.perform(multipart("/api/strategies/upload").file(file))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("EMA")));
    }

    @Test
    void getAllStrategiesReturnsList() throws Exception {
        when(strategyFileService.getAllStrategies()).thenReturn(List.of(strategyResponse(1L)));

        mockMvc.perform(get("/api/strategies"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].fileName", is("ema.py")));
    }

    @Test
    void getStrategyByIdReturnsEntity() throws Exception {
        when(strategyFileService.getStrategyById(1L)).thenReturn(strategyResponse(1L));

        mockMvc.perform(get("/api/strategies/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)));
    }

    private StrategyResponse strategyResponse(Long id) {
        return new StrategyResponse(
            id,
            "EMA",
            "ema.py",
            StrategyFileEntity.StrategyStatus.VALID,
            null,
            Map.of("period", Map.of("type", "integer")),
            Instant.parse("2024-01-01T00:00:00Z")
        );
    }
}
