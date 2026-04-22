package com.example.back.strategies.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.back.imports.client.PythonParserClient;
import com.example.back.strategies.dto.StrategyValidationResponse;
import com.example.back.strategies.entity.StrategyFileEntity;
import com.example.back.strategies.repository.StrategyFileRepository;
import com.example.back.support.TestAuth;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class StrategyFileServiceTest {

    @Mock
    private StrategyFileRepository strategyFileRepository;

    @Mock
    private PythonParserClient pythonParserClient;

    @SuppressWarnings("unused")
    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private StrategyFileService strategyFileService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        TestAuth.setAuthenticatedUser();
    }

    @AfterEach
    void tearDown() {
        TestAuth.clearAuthentication();
    }

    @Test
    void uploadStrategyStoresFileAndAppliesValidationResult() throws Exception {
        ReflectionTestUtils.setField(strategyFileService, "storagePath", tempDir.toString());
        ReflectionTestUtils.setField(strategyFileService, "objectMapper", new ObjectMapper());
        when(strategyFileRepository.save(any(StrategyFileEntity.class))).thenAnswer(invocation -> {
            StrategyFileEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(1L);
            }
            return entity;
        });
        when(pythonParserClient.validateStrategy(any())).thenReturn(
            new StrategyValidationResponse(true, "EMA", Map.of("period", Map.of("type", "integer")), null)
        );

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "ema.py",
            "text/x-python",
            "class Strategy:\n    pass\n".getBytes()
        );

        var response = strategyFileService.uploadStrategy(file);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("EMA");
        assertThat(response.status()).isEqualTo(StrategyFileEntity.StrategyStatus.VALID);
        assertThat(Files.list(tempDir)).hasSize(1);
        verify(pythonParserClient).validateStrategy(any());
    }

    @Test
    void uploadStrategyRejectsNonPythonFiles() {
        MockMultipartFile file = new MockMultipartFile("file", "notes.txt", "text/plain", "x".getBytes());

        assertThatThrownBy(() -> strategyFileService.uploadStrategy(file))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getAllStrategiesMapsEntities() {
        StrategyFileEntity entity = new StrategyFileEntity();
        entity.setId(1L);
        entity.setUserId(TestAuth.USER_ID);
        entity.setName("EMA");
        entity.setFileName("ema.py");
        entity.setStoragePath("/tmp/ema.py");
        entity.setStatus(StrategyFileEntity.StrategyStatus.VALID);
        entity.setParametersSchemaJson("{\"period\":{\"type\":\"integer\"}}");
        entity.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        when(strategyFileRepository.findAllByUserIdOrderByCreatedAtDesc(TestAuth.USER_ID)).thenReturn(List.of(entity));

        var strategies = strategyFileService.getAllStrategies();

        assertThat(strategies).hasSize(1);
        assertThat(strategies.get(0).parametersSchema()).containsKey("period");
    }

    @Test
    void getStrategyByIdFailsWhenEntityIsMissing() {
        when(strategyFileRepository.findByIdAndUserId(1L, TestAuth.USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> strategyFileService.getStrategyById(1L))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
