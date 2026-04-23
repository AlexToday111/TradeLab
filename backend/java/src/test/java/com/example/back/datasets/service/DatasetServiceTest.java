package com.example.back.datasets.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.example.back.datasets.dto.RenameDatasetRequest;
import com.example.back.datasets.entity.DatasetEntity;
import com.example.back.datasets.entity.DatasetQualityReportEntity;
import com.example.back.datasets.entity.DatasetSnapshotEntity;
import com.example.back.datasets.repository.DatasetQualityReportRepository;
import com.example.back.datasets.repository.DatasetRepository;
import com.example.back.datasets.repository.DatasetSnapshotRepository;
import com.example.back.imports.dto.ImportCandlesResponse;
import com.example.back.support.TestAuth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class DatasetServiceTest {

    @Mock
    private DatasetRepository datasetRepository;

    @Mock
    private DatasetSnapshotRepository datasetSnapshotRepository;

    @Mock
    private DatasetQualityReportRepository datasetQualityReportRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private DatasetService datasetService;

    @BeforeEach
    void setUp() {
        TestAuth.setAuthenticatedUser();
        datasetService = new DatasetService(
                datasetRepository,
                datasetSnapshotRepository,
                datasetQualityReportRepository,
                objectMapper
        );
        lenient().when(datasetSnapshotRepository.save(any(DatasetSnapshotEntity.class))).thenAnswer(invocation -> {
            DatasetSnapshotEntity snapshot = invocation.getArgument(0);
            if (snapshot.getId() == null) {
                snapshot.setId(1L);
            }
            return snapshot;
        });
        lenient().when(datasetSnapshotRepository.findByDatasetIdAndDatasetVersion(any(String.class), any(String.class)))
                .thenReturn(Optional.empty());
        lenient().when(datasetQualityReportRepository.save(any(DatasetQualityReportEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        TestAuth.clearAuthentication();
    }

    @Test
    void getDatasetsReadsStoredPayloads() {
        DatasetEntity entity = createEntity("dataset-1", "Dataset 1", "{\"id\":\"dataset-1\",\"name\":\"Dataset 1\"}");
        when(datasetRepository.findAllByUserIdOrderByCreatedAtDesc(TestAuth.USER_ID)).thenReturn(List.of(entity));

        var datasets = datasetService.getDatasets();

        assertThat(datasets).hasSize(1);
        assertThat(datasets.get(0).path("id").asText()).isEqualTo("dataset-1");
    }

    @Test
    void createDatasetValidatesPayloadAndPersistsEntity() {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("id", "dataset-1");
        payload.put("name", "Dataset 1");
        when(datasetRepository.findById("dataset-1")).thenReturn(Optional.empty());
        when(datasetRepository.findByIdAndUserId("dataset-1", TestAuth.USER_ID)).thenReturn(Optional.empty());
        when(datasetRepository.save(any(DatasetEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = datasetService.createDataset(payload);

        assertThat(result.path("name").asText()).isEqualTo("Dataset 1");
    }

    @Test
    void renameDatasetUpdatesPayloadAndEntityName() {
        DatasetEntity entity = createEntity("dataset-1", "Old name", "{\"id\":\"dataset-1\",\"name\":\"Old name\"}");
        when(datasetRepository.findByIdAndUserId("dataset-1", TestAuth.USER_ID)).thenReturn(Optional.of(entity));
        when(datasetRepository.save(any(DatasetEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = datasetService.renameDataset("dataset-1", new RenameDatasetRequest("  New Name  "));

        assertThat(result.path("name").asText()).isEqualTo("New Name");
        assertThat(entity.getName()).isEqualTo("New Name");
    }

    @Test
    void renameDatasetRejectsBlankName() {
        DatasetEntity entity = createEntity("dataset-1", "Old name", "{\"id\":\"dataset-1\",\"name\":\"Old name\"}");
        when(datasetRepository.findByIdAndUserId("dataset-1", TestAuth.USER_ID)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> datasetService.renameDataset("dataset-1", new RenameDatasetRequest("   ")))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void duplicateDatasetCreatesNewRecordWithAdjustedPayload() {
        DatasetEntity entity = createEntity("dataset-1", "Dataset 1", "{\"id\":\"dataset-1\",\"name\":\"Dataset 1\"}");
        when(datasetRepository.findById(any(String.class))).thenReturn(Optional.empty());
        when(datasetRepository.findByIdAndUserId("dataset-1", TestAuth.USER_ID)).thenReturn(Optional.of(entity));
        when(datasetRepository.save(any(DatasetEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = datasetService.duplicateDataset("dataset-1");

        assertThat(result.path("id").asText()).startsWith("dataset-");
        assertThat(result.path("id").asText()).isNotEqualTo("dataset-1");
        assertThat(result.path("name").asText()).startsWith("Dataset 1");
    }

    @Test
    void deleteDatasetFailsWhenDatasetIsMissing() {
        when(datasetRepository.findByIdAndUserId("missing", TestAuth.USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> datasetService.deleteDataset("missing"))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void upsertImportedDatasetPersistsMetadataFields() {
        ImportCandlesResponse response = new ImportCandlesResponse();
        response.setStatus("success");
        response.setExchange("binance");
        response.setSymbol("BTCUSDT");
        response.setInterval("1h");
        response.setDataset(Map.ofEntries(
                Map.entry("datasetId", "dataset-1"),
                Map.entry("name", "Binance BTCUSDT 1h"),
                Map.entry("source", "binance"),
                Map.entry("symbol", "BTCUSDT"),
                Map.entry("timeframe", "1h"),
                Map.entry("importedAt", "2024-01-02T00:00:00Z"),
                Map.entry("rowsCount", 100),
                Map.entry("startAt", "2024-01-01T00:00:00Z"),
                Map.entry("endAt", "2024-01-02T00:00:00Z"),
                Map.entry("version", "abc123"),
                Map.entry("fingerprint", "abc123"),
                Map.entry("qualityFlags", List.of()),
                Map.entry("lineage", Map.of("rawRows", 100))
        ));
        when(datasetRepository.findById("dataset-1")).thenReturn(Optional.empty());
        when(datasetRepository.findByIdAndUserId("dataset-1", TestAuth.USER_ID)).thenReturn(Optional.empty());
        when(datasetRepository.save(any(DatasetEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JsonNode result = datasetService.upsertImportedDataset(response);

        assertThat(result.path("id").asText()).isEqualTo("dataset-1");
        assertThat(result.path("source").asText()).isEqualTo("binance");
        assertThat(result.path("rowsCount").asInt()).isEqualTo(100);
    }

    private DatasetEntity createEntity(String id, String name, String payload) {
        DatasetEntity entity = new DatasetEntity();
        entity.setId(id);
        entity.setUserId(TestAuth.USER_ID);
        entity.setName(name);
        entity.setPayload(payload);
        entity.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        entity.setUpdatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        return entity;
    }
}
