package com.example.back.strategies.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.back.strategies.dto.StrategyPresetResponse;
import com.example.back.strategies.dto.StrategyResponse;
import com.example.back.strategies.dto.StrategyVersionResponse;
import com.example.back.strategies.entity.StrategyFileEntity;
import com.example.back.strategies.entity.StrategyVersionEntity;
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

    @Test
    void createStrategyVersionReturnsCreatedResponse() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "ema.py", "text/x-python", "x".getBytes());
        when(strategyFileService.createStrategyVersion(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(strategyVersionResponse(10L));

        mockMvc.perform(multipart("/api/strategies/1/versions").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.validationStatus", is("VALID")));
    }

    @Test
    void createPresetReturnsCreatedResponse() throws Exception {
        when(strategyFileService.createPreset(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(strategyPresetResponse(7L));

        mockMvc.perform(post("/api/strategies/1/presets")
                        .contentType("application/json")
                        .content("""
                                {"name":"Default","presetPayload":{"period":20}}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(7)))
                .andExpect(jsonPath("$.presetPayload.period", is(20)));
    }

    @Test
    void deletePresetReturnsNoContent() throws Exception {
        doNothing().when(strategyFileService).deletePreset(7L);

        mockMvc.perform(delete("/api/strategy-presets/7"))
                .andExpect(status().isNoContent());
    }

    private StrategyResponse strategyResponse(Long id) {
        return new StrategyResponse(
            id,
            1L,
            "ema",
            "EMA",
            "EMA strategy",
            "BACKTEST",
            StrategyFileEntity.StrategyLifecycleStatus.ACTIVE,
            "1",
            10L,
            "ema.py",
            StrategyFileEntity.StrategyStatus.VALID,
            null,
            Map.of("period", Map.of("type", "integer")),
            Map.of("category", "MOMENTUM"),
            List.of("ema"),
            "text/x-python",
            10L,
            "abc123",
            Instant.parse("2024-01-01T00:00:00Z"),
            Instant.parse("2024-01-01T00:00:00Z"),
            Instant.parse("2024-01-01T00:00:00Z")
        );
    }

    private StrategyVersionResponse strategyVersionResponse(Long id) {
        return new StrategyVersionResponse(
                id,
                1L,
                "1",
                "/tmp/ema.py",
                "ema.py",
                "text/x-python",
                10L,
                "abc123",
                StrategyVersionEntity.ValidationStatus.VALID,
                Map.of("status", "VALID"),
                Map.of("period", Map.of("type", "integer")),
                Map.of("category", "MOMENTUM"),
                "python-execution-engine/0.3.0-alpha.1",
                Instant.parse("2024-01-01T00:00:00Z"),
                1L
        );
    }

    private StrategyPresetResponse strategyPresetResponse(Long id) {
        return new StrategyPresetResponse(
                id,
                1L,
                1L,
                "Default",
                Map.of("period", 20),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z")
        );
    }
}
