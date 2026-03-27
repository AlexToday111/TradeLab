package com.example.back.datasets.service;

import com.example.back.datasets.dto.RenameDatasetRequest;
import com.example.back.datasets.entity.DatasetEntity;
import com.example.back.datasets.repository.DatasetRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DatasetService {

    private final DatasetRepository datasetRepository;
    private final ObjectMapper objectMapper;

    public DatasetService(DatasetRepository datasetRepository, ObjectMapper objectMapper) {
        this.datasetRepository = datasetRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<JsonNode> getDatasets() {
        return datasetRepository.findAllByOrderByCreatedAtDesc().stream().map(this::readPayload).toList();
    }

    @Transactional
    public JsonNode createDataset(JsonNode payload) {
        ObjectNode normalizedPayload = validatePayload(payload);

        DatasetEntity entity = new DatasetEntity();
        entity.setId(normalizedPayload.path("id").asText());
        entity.setName(normalizedPayload.path("name").asText());
        entity.setPayload(writePayload(normalizedPayload));

        return readPayload(datasetRepository.save(entity));
    }

    @Transactional
    public JsonNode renameDataset(String id, RenameDatasetRequest request) {
        DatasetEntity entity = datasetRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found"));

        String nextName = request.name() == null ? "" : request.name().trim();
        if (nextName.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dataset name is required");
        }

        ObjectNode payload = asObjectNode(readPayload(entity));
        payload.put("name", nextName);
        entity.setName(nextName);
        entity.setPayload(writePayload(payload));

        return readPayload(datasetRepository.save(entity));
    }

    @Transactional
    public JsonNode duplicateDataset(String id) {
        DatasetEntity source = datasetRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found"));

        ObjectNode payload = asObjectNode(readPayload(source));
        String newId = "dataset-" + UUID.randomUUID();
        String newName = payload.path("name").asText("Dataset") + " (копия)";
        payload.put("id", newId);
        payload.put("name", newName);

        DatasetEntity duplicate = new DatasetEntity();
        duplicate.setId(newId);
        duplicate.setName(newName);
        duplicate.setPayload(writePayload(payload));

        return readPayload(datasetRepository.save(duplicate));
    }

    @Transactional
    public void deleteDataset(String id) {
        if (!datasetRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found");
        }
        datasetRepository.deleteById(id);
    }

    private ObjectNode validatePayload(JsonNode payload) {
        if (payload == null || !payload.isObject()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dataset payload must be a JSON object");
        }

        ObjectNode objectNode = (ObjectNode) payload.deepCopy();
        String id = objectNode.path("id").asText("").trim();
        String name = objectNode.path("name").asText("").trim();

        if (id.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dataset id is required");
        }
        if (name.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dataset name is required");
        }
        return objectNode;
    }

    private ObjectNode asObjectNode(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            return objectNode;
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Dataset payload is corrupted");
    }

    private JsonNode readPayload(DatasetEntity entity) {
        try {
            return objectMapper.readTree(entity.getPayload());
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse dataset payload", ex);
        }
    }

    private String writePayload(JsonNode payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize dataset payload", ex);
        }
    }
}
