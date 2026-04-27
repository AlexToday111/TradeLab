package com.example.back.strategies.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.back.imports.client.PythonParserClient;
import com.example.back.strategies.dto.CreateStrategyPresetRequest;
import com.example.back.strategies.dto.StrategyValidationResponse;
import com.example.back.strategies.entity.StrategyFileEntity;
import com.example.back.strategies.entity.StrategyParameterPresetEntity;
import com.example.back.strategies.entity.StrategyVersionEntity;
import com.example.back.strategies.repository.StrategyFileRepository;
import com.example.back.strategies.repository.StrategyParameterPresetRepository;
import com.example.back.strategies.repository.StrategyVersionRepository;
import com.example.back.support.TestAuth;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
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
    private StrategyVersionRepository strategyVersionRepository;

    @Mock
    private StrategyParameterPresetRepository presetRepository;

    @Mock
    private PythonParserClient pythonParserClient;

    private StrategyFileService strategyFileService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        TestAuth.setAuthenticatedUser();
        strategyFileService = new StrategyFileService(
                strategyFileRepository,
                strategyVersionRepository,
                presetRepository,
                pythonParserClient,
                new ObjectMapper()
        );
        ReflectionTestUtils.setField(strategyFileService, "storagePath", tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        TestAuth.clearAuthentication();
    }

    @Test
    void uploadStrategyStoresFileAndAppliesValidationResult() throws Exception {
        AtomicReference<StrategyFileEntity> savedStrategy = new AtomicReference<>();
        when(strategyFileRepository.findByUserIdAndStrategyKey(anyLong(), anyString())).thenReturn(Optional.empty());
        when(strategyFileRepository.saveAndFlush(any(StrategyFileEntity.class))).thenAnswer(invocation -> {
            StrategyFileEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(1L);
            }
            entity.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
            entity.setUpdatedAt(Instant.parse("2024-01-01T00:00:00Z"));
            savedStrategy.set(entity);
            return entity;
        });
        when(strategyVersionRepository.findAllByStrategyIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(strategyVersionRepository.saveAndFlush(any(StrategyVersionEntity.class))).thenAnswer(invocation -> {
            StrategyVersionEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(10L);
            }
            if (entity.getCreatedAt() == null) {
                entity.setCreatedAt(Instant.parse("2024-01-01T00:00:01Z"));
            }
            return entity;
        });
        when(strategyFileRepository.save(any(StrategyFileEntity.class))).thenAnswer(invocation -> {
            StrategyFileEntity entity = invocation.getArgument(0);
            savedStrategy.set(entity);
            return entity;
        });
        when(strategyFileRepository.findById(1L)).thenAnswer(invocation -> Optional.of(savedStrategy.get()));
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
        assertThat(response.latestVersionId()).isEqualTo(10L);
        assertThat(response.checksum()).isNotBlank();
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
        entity.setStrategyKey("ema");
        entity.setName("EMA");
        entity.setFileName("ema.py");
        entity.setStoragePath("/tmp/ema.py");
        entity.setStatus(StrategyFileEntity.StrategyStatus.VALID);
        entity.setLifecycleStatus(StrategyFileEntity.StrategyLifecycleStatus.ACTIVE);
        entity.setParametersSchemaJson("{\"period\":{\"type\":\"integer\"}}");
        entity.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        entity.setUpdatedAt(Instant.parse("2024-01-01T00:00:00Z"));
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

    @Test
    void createPresetPersistsPayloadForOwnedStrategy() {
        StrategyFileEntity strategy = new StrategyFileEntity();
        strategy.setId(1L);
        strategy.setUserId(TestAuth.USER_ID);
        when(strategyFileRepository.findByIdAndUserId(1L, TestAuth.USER_ID)).thenReturn(Optional.of(strategy));
        when(presetRepository.save(any(StrategyParameterPresetEntity.class))).thenAnswer(invocation -> {
            StrategyParameterPresetEntity entity = invocation.getArgument(0);
            entity.setId(7L);
            entity.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
            entity.setUpdatedAt(Instant.parse("2024-01-01T00:00:00Z"));
            return entity;
        });

        var response = strategyFileService.createPreset(
                1L,
                new CreateStrategyPresetRequest("Default", Map.of("period", 20))
        );

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.presetPayload()).containsEntry("period", 20);
    }

    @Test
    void activateVersionRejectsInvalidValidationStatus() {
        StrategyVersionEntity version = new StrategyVersionEntity();
        version.setId(10L);
        version.setStrategyId(1L);
        version.setVersion("1");
        version.setValidationStatus(StrategyVersionEntity.ValidationStatus.INVALID);
        when(strategyVersionRepository.findOwnedById(10L, TestAuth.USER_ID)).thenReturn(Optional.of(version));

        assertThatThrownBy(() -> strategyFileService.activateStrategyVersion(10L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
