package com.example.back.datasets.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.back.datasets.dto.RenameDatasetRequest;
import com.example.back.datasets.entity.DatasetEntity;
import com.example.back.datasets.repository.DatasetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class DatasetServiceTest {

    @Mock
    private DatasetRepository datasetRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private DatasetService datasetService;

    @BeforeEach
    void setUp() {
        datasetService = new DatasetService(datasetRepository, objectMapper);
    }

    @Test
    void getDatasetsReadsStoredPayloads() {
        DatasetEntity entity = createEntity("dataset-1", "Dataset 1", "{\"id\":\"dataset-1\",\"name\":\"Dataset 1\"}");
        when(datasetRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(entity));

        var datasets = datasetService.getDatasets();

        assertThat(datasets).hasSize(1);
        assertThat(datasets.get(0).path("id").asText()).isEqualTo("dataset-1");
    }

    @Test
    void createDatasetValidatesPayloadAndPersistsEntity() {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("id", "dataset-1");
        payload.put("name", "Dataset 1");
        when(datasetRepository.save(any(DatasetEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = datasetService.createDataset(payload);

        assertThat(result.path("name").asText()).isEqualTo("Dataset 1");
    }

    @Test
    void renameDatasetUpdatesPayloadAndEntityName() {
        DatasetEntity entity = createEntity("dataset-1", "Old name", "{\"id\":\"dataset-1\",\"name\":\"Old name\"}");
        when(datasetRepository.findById("dataset-1")).thenReturn(Optional.of(entity));
        when(datasetRepository.save(any(DatasetEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = datasetService.renameDataset("dataset-1", new RenameDatasetRequest("  New Name  "));

        assertThat(result.path("name").asText()).isEqualTo("New Name");
        assertThat(entity.getName()).isEqualTo("New Name");
    }

    @Test
    void renameDatasetRejectsBlankName() {
        DatasetEntity entity = createEntity("dataset-1", "Old name", "{\"id\":\"dataset-1\",\"name\":\"Old name\"}");
        when(datasetRepository.findById("dataset-1")).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> datasetService.renameDataset("dataset-1", new RenameDatasetRequest("   ")))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void duplicateDatasetCreatesNewRecordWithAdjustedPayload() {
        DatasetEntity entity = createEntity("dataset-1", "Dataset 1", "{\"id\":\"dataset-1\",\"name\":\"Dataset 1\"}");
        when(datasetRepository.findById("dataset-1")).thenReturn(Optional.of(entity));
        when(datasetRepository.save(any(DatasetEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = datasetService.duplicateDataset("dataset-1");

        assertThat(result.path("id").asText()).startsWith("dataset-");
        assertThat(result.path("id").asText()).isNotEqualTo("dataset-1");
        assertThat(result.path("name").asText()).startsWith("Dataset 1");
    }

    @Test
    void deleteDatasetFailsWhenDatasetIsMissing() {
        when(datasetRepository.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> datasetService.deleteDataset("missing"))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    private DatasetEntity createEntity(String id, String name, String payload) {
        DatasetEntity entity = new DatasetEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setPayload(payload);
        entity.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        entity.setUpdatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        return entity;
    }
}
