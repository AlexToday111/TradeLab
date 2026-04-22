package com.example.back.datasets.service;

import com.example.back.auth.security.AuthContext;
import com.example.back.datasets.dto.RenameDatasetRequest;
import com.example.back.datasets.entity.DatasetEntity;
import com.example.back.datasets.repository.DatasetRepository;
import com.example.back.imports.dto.ImportCandlesResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
        Long userId = AuthContext.requireUserId();
        return datasetRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream().map(this::readPayload).toList();
    }

    @Transactional
    public JsonNode createDataset(JsonNode payload) {
        Long userId = AuthContext.requireUserId();
        ObjectNode normalizedPayload = validatePayload(payload);
        String datasetId = resolveDatasetId(normalizedPayload.path("id").asText(), userId);
        normalizedPayload.put("id", datasetId);
        DatasetEntity entity = datasetRepository.findByIdAndUserId(datasetId, userId).orElseGet(DatasetEntity::new);
        entity.setUserId(userId);
        applyPayload(entity, normalizedPayload);
        return readPayload(datasetRepository.save(entity));
    }

    @Transactional
    public JsonNode renameDataset(String id, RenameDatasetRequest request) {
        Long userId = AuthContext.requireUserId();
        DatasetEntity entity = datasetRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found"));

        String nextName = request.name() == null ? "" : request.name().trim();
        if (nextName.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dataset name is required");
        }

        ObjectNode payload = asObjectNode(readPayload(entity));
        payload.put("name", nextName);
        applyPayload(entity, payload);

        return readPayload(datasetRepository.save(entity));
    }

    @Transactional
    public JsonNode duplicateDataset(String id) {
        Long userId = AuthContext.requireUserId();
        DatasetEntity source = datasetRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found"));

        ObjectNode payload = asObjectNode(readPayload(source));
        String newId = resolveDatasetId("dataset-" + UUID.randomUUID(), userId);
        String newName = payload.path("name").asText("Dataset") + " (copy)";
        payload.put("id", newId);
        payload.put("name", newName);

        DatasetEntity duplicate = new DatasetEntity();
        duplicate.setUserId(userId);
        applyPayload(duplicate, payload);

        return readPayload(datasetRepository.save(duplicate));
    }

    @Transactional
    public JsonNode upsertImportedDataset(ImportCandlesResponse response) {
        Long userId = AuthContext.requireUserId();
        ObjectNode payload = normalizeImportedDatasetPayload(response);
        String datasetId = resolveDatasetId(payload.path("id").asText(), userId);
        payload.put("id", datasetId);
        DatasetEntity entity = datasetRepository.findByIdAndUserId(datasetId, userId).orElseGet(DatasetEntity::new);
        entity.setUserId(userId);
        applyPayload(entity, payload);
        return readPayload(datasetRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Optional<String> findDatasetIdForRange(
            String source,
            String symbol,
            String interval,
            Instant from,
            Instant to
    ) {
        Long userId = AuthContext.requireUserId();
        return datasetRepository
                .findFirstByUserIdAndSourceIgnoreCaseAndSymbolIgnoreCaseAndIntervalAndStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByImportedAtDesc(
                        userId,
                        source,
                        symbol,
                        interval,
                        from,
                        to
                )
                .map(DatasetEntity::getId);
    }

    @Transactional
    public void deleteDataset(String id) {
        Long userId = AuthContext.requireUserId();
        DatasetEntity entity = datasetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found"));
        datasetRepository.delete(entity);
    }

    private String resolveDatasetId(String requestedId, Long userId) {
        Optional<DatasetEntity> existing = datasetRepository.findById(requestedId);
        if (existing.isEmpty()) {
            return requestedId;
        }
        return userId.equals(existing.get().getUserId()) ? requestedId : "dataset-" + UUID.randomUUID();
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
        objectNode.put("id", id);
        objectNode.put("name", name);
        return objectNode;
    }

    private void applyPayload(DatasetEntity entity, ObjectNode payload) {
        ObjectNode normalizedPayload = validatePayload(payload);
        entity.setId(normalizedPayload.path("id").asText());
        entity.setName(normalizedPayload.path("name").asText());
        entity.setSource(textOrNull(normalizedPayload, "source"));
        entity.setSymbol(textOrNull(normalizedPayload, "symbol"));
        entity.setInterval(textOrNull(normalizedPayload, "timeframe", "interval"));
        entity.setImportedAt(instantOrNull(normalizedPayload, "importedAt"));
        entity.setRowsCount(intOrNull(normalizedPayload, "rowsCount"));
        entity.setStartAt(instantOrNull(normalizedPayload, "startAt"));
        entity.setEndAt(instantOrNull(normalizedPayload, "endAt"));
        entity.setVersion(textOrNull(normalizedPayload, "version"));
        entity.setFingerprint(textOrNull(normalizedPayload, "fingerprint"));
        entity.setQualityFlagsJson(writePayload(normalizedPayload.path("qualityFlags")));
        entity.setLineageJson(writePayload(normalizedPayload.path("lineage")));
        entity.setPayload(writePayload(normalizedPayload));
    }

    private ObjectNode normalizeImportedDatasetPayload(ImportCandlesResponse response) {
        if (response.getDataset() == null || response.getDataset().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Import response does not contain dataset metadata");
        }

        JsonNode tree = objectMapper.valueToTree(response.getDataset());
        ObjectNode payload = asObjectNode(tree);
        if (payload.path("id").asText("").isBlank()) {
            payload.put("id", payload.path("datasetId").asText());
        }
        if (payload.path("name").asText("").isBlank()) {
            payload.put(
                    "name",
                    "%s %s %s".formatted(
                            response.getExchange(),
                            response.getSymbol(),
                            response.getInterval()
                    ).trim()
            );
        }
        if (payload.path("source").asText("").isBlank()) {
            payload.put("source", response.getExchange());
        }
        if (payload.path("symbol").asText("").isBlank()) {
            payload.put("symbol", response.getSymbol());
        }
        if (payload.path("timeframe").asText("").isBlank()) {
            payload.put("timeframe", response.getInterval());
        }
        return payload;
    }

    private String textOrNull(ObjectNode payload, String... fields) {
        for (String field : fields) {
            String value = payload.path(field).asText("").trim();
            if (!value.isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private Instant instantOrNull(ObjectNode payload, String field) {
        String value = payload.path(field).asText("").trim();
        if (value.isEmpty()) {
            return null;
        }
        return Instant.parse(value);
    }

    private Integer intOrNull(ObjectNode payload, String field) {
        return payload.hasNonNull(field) ? payload.path(field).asInt() : null;
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
