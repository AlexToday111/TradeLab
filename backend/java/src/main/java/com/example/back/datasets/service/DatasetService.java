package com.example.back.datasets.service;

import com.example.back.auth.security.AuthContext;
import com.example.back.backtest.exception.BacktestResourceNotFoundException;
import com.example.back.datasets.dto.DatasetDetailsResponse;
import com.example.back.datasets.dto.DatasetQualityReportResponse;
import com.example.back.datasets.dto.DatasetSnapshotResponse;
import com.example.back.datasets.dto.RenameDatasetRequest;
import com.example.back.datasets.entity.DatasetEntity;
import com.example.back.datasets.entity.DatasetQualityReportEntity;
import com.example.back.datasets.entity.DatasetSnapshotEntity;
import com.example.back.datasets.repository.DatasetQualityReportRepository;
import com.example.back.datasets.repository.DatasetRepository;
import com.example.back.datasets.repository.DatasetSnapshotRepository;
import com.example.back.imports.dto.ImportCandlesResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DatasetService {

    private final DatasetRepository datasetRepository;
    private final DatasetSnapshotRepository datasetSnapshotRepository;
    private final DatasetQualityReportRepository datasetQualityReportRepository;
    private final ObjectMapper objectMapper;

    public DatasetService(
            DatasetRepository datasetRepository,
            DatasetSnapshotRepository datasetSnapshotRepository,
            DatasetQualityReportRepository datasetQualityReportRepository,
            ObjectMapper objectMapper
    ) {
        this.datasetRepository = datasetRepository;
        this.datasetSnapshotRepository = datasetSnapshotRepository;
        this.datasetQualityReportRepository = datasetQualityReportRepository;
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
        DatasetEntity saved = datasetRepository.save(entity);
        syncSnapshotAndQuality(saved);
        return readPayload(saved);
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

        DatasetEntity saved = datasetRepository.save(entity);
        syncSnapshotAndQuality(saved);
        return readPayload(saved);
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

        DatasetEntity saved = datasetRepository.save(duplicate);
        syncSnapshotAndQuality(saved);
        return readPayload(saved);
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
        DatasetEntity saved = datasetRepository.save(entity);
        syncSnapshotAndQuality(saved);
        return readPayload(saved);
    }

    @Transactional(readOnly = true)
    public DatasetDetailsResponse getDatasetDetails(String id) {
        DatasetEntity dataset = findOwnedDataset(id);
        DatasetSnapshotEntity latestSnapshot = datasetSnapshotRepository
                .findFirstByDatasetIdOrderByCreatedAtDesc(id)
                .orElse(null);
        DatasetQualityReportEntity latestQuality = datasetQualityReportRepository
                .findFirstByDatasetIdOrderByCheckedAtDesc(id)
                .orElse(null);
        return DatasetDetailsResponse.builder()
                .dataset(readPayload(dataset))
                .latestSnapshot(toSnapshotResponse(latestSnapshot))
                .latestQualityReport(toQualityReportResponse(latestQuality))
                .build();
    }

    @Transactional(readOnly = true)
    public List<DatasetSnapshotResponse> getDatasetVersions(String id) {
        findOwnedDataset(id);
        return datasetSnapshotRepository.findAllByDatasetIdOrderByCreatedAtDesc(id).stream()
                .map(this::toSnapshotResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DatasetQualityReportResponse> getDatasetQuality(String id) {
        findOwnedDataset(id);
        return datasetQualityReportRepository.findAllByDatasetIdOrderByCheckedAtDesc(id).stream()
                .map(this::toQualityReportResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DatasetSnapshotResponse getDatasetSnapshot(Long snapshotId) {
        DatasetSnapshotEntity snapshot = datasetSnapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new BacktestResourceNotFoundException("Dataset snapshot not found: " + snapshotId));
        findOwnedDataset(snapshot.getDatasetId());
        return toSnapshotResponse(snapshot);
    }

    @Transactional(readOnly = true)
    public Optional<DatasetSnapshotEntity> findLatestSnapshot(String datasetId) {
        return datasetSnapshotRepository.findFirstByDatasetIdOrderByCreatedAtDesc(datasetId);
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
        entity.setVersion(resolveDatasetVersion(normalizedPayload));
        entity.setFingerprint(textOrNull(normalizedPayload, "fingerprint"));
        entity.setQualityFlagsJson(writePayload(normalizedPayload.path("qualityFlags")));
        entity.setLineageJson(writePayload(normalizedPayload.path("lineage")));
        entity.setPayload(writePayload(normalizedPayload));
    }

    private void syncSnapshotAndQuality(DatasetEntity dataset) {
        ObjectNode payload = asObjectNode(readPayload(dataset));
        DatasetSnapshotEntity snapshot = datasetSnapshotRepository
                .findByDatasetIdAndDatasetVersion(dataset.getId(), dataset.getVersion())
                .orElseGet(DatasetSnapshotEntity::new);
        snapshot.setDatasetId(dataset.getId());
        snapshot.setDatasetVersion(dataset.getVersion());
        snapshot.setSourceExchange(dataset.getSource());
        snapshot.setSymbol(dataset.getSymbol());
        snapshot.setInterval(dataset.getInterval());
        snapshot.setStartTime(dataset.getStartAt());
        snapshot.setEndTime(dataset.getEndAt());
        snapshot.setRowCount(dataset.getRowsCount());
        snapshot.setChecksum(dataset.getFingerprint());
        snapshot.setSourceMetadataJson(writePayload(buildSourceMetadata(dataset, payload)));
        snapshot.setCoverageMetadataJson(writePayload(buildCoverageMetadata(dataset)));
        DatasetSnapshotEntity savedSnapshot = datasetSnapshotRepository.save(snapshot);

        DatasetQualityReportEntity quality = new DatasetQualityReportEntity();
        quality.setDatasetId(dataset.getId());
        quality.setDatasetSnapshotId(savedSnapshot.getId());
        quality.setQualityStatus(resolveQualityStatus(payload));
        quality.setIssuesJson(writePayload(resolveQualityIssues(payload)));
        quality.setCheckedAt(resolveCheckedAt(payload));
        datasetQualityReportRepository.save(quality);
    }

    private ObjectNode buildSourceMetadata(DatasetEntity dataset, ObjectNode payload) {
        ObjectNode source = objectMapper.createObjectNode();
        source.put("source", dataset.getSource());
        source.put("symbol", dataset.getSymbol());
        source.put("interval", dataset.getInterval());
        source.set("lineage", payload.path("lineage").deepCopy());
        if (payload.has("backendRequest")) {
            source.set("backendRequest", payload.path("backendRequest").deepCopy());
        }
        return source;
    }

    private ObjectNode buildCoverageMetadata(DatasetEntity dataset) {
        ObjectNode coverage = objectMapper.createObjectNode();
        coverage.put("startTime", dataset.getStartAt() == null ? null : dataset.getStartAt().toString());
        coverage.put("endTime", dataset.getEndAt() == null ? null : dataset.getEndAt().toString());
        coverage.put("rowCount", dataset.getRowsCount());
        coverage.put("sourceExchange", dataset.getSource());
        coverage.put("symbol", dataset.getSymbol());
        coverage.put("interval", dataset.getInterval());
        return coverage;
    }

    private String resolveDatasetVersion(ObjectNode payload) {
        String version = textOrNull(payload, "version");
        if (version != null) {
            return version;
        }
        String fingerprint = textOrNull(payload, "fingerprint", "pipelineHash");
        if (fingerprint != null) {
            return fingerprint;
        }
        return "manual-" + payload.path("id").asText();
    }

    private String resolveQualityStatus(ObjectNode payload) {
        JsonNode qualityReport = payload.path("qualityReport");
        String reportStatus = qualityReport.path("status").asText("").trim();
        if (!reportStatus.isEmpty()) {
            return reportStatus.toUpperCase();
        }
        String directStatus = payload.path("qualityStatus").asText("").trim();
        if (!directStatus.isEmpty()) {
            return directStatus.toUpperCase();
        }
        JsonNode qualityFlags = payload.path("qualityFlags");
        return qualityFlags.isArray() && !qualityFlags.isEmpty() ? "WARNING" : "OK";
    }

    private JsonNode resolveQualityIssues(ObjectNode payload) {
        JsonNode qualityReport = payload.path("qualityReport");
        if (qualityReport.has("issues")) {
            return qualityReport.path("issues").deepCopy();
        }
        JsonNode qualityFlags = payload.path("qualityFlags");
        if (qualityFlags.isArray()) {
            List<Map<String, Object>> issues = new ArrayList<>();
            qualityFlags.forEach(flag -> issues.add(Map.of(
                    "code", flag.asText(),
                    "severity", "WARNING",
                    "message", flag.asText()
            )));
            return objectMapper.valueToTree(issues);
        }
        return objectMapper.createArrayNode();
    }

    private Instant resolveCheckedAt(ObjectNode payload) {
        JsonNode qualityReport = payload.path("qualityReport");
        String checkedAt = qualityReport.path("checkedAt").asText("").trim();
        if (checkedAt.isEmpty()) {
            return Instant.now();
        }
        return Instant.parse(checkedAt);
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

    private Object readJsonObject(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(payload, Object.class);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse dataset JSON", ex);
        }
    }

    private DatasetEntity findOwnedDataset(String id) {
        Long userId = AuthContext.requireUserId();
        return datasetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found"));
    }

    private DatasetSnapshotResponse toSnapshotResponse(DatasetSnapshotEntity entity) {
        if (entity == null) {
            return null;
        }
        return DatasetSnapshotResponse.builder()
                .id(entity.getId())
                .datasetId(entity.getDatasetId())
                .datasetVersion(entity.getDatasetVersion())
                .sourceExchange(entity.getSourceExchange())
                .symbol(entity.getSymbol())
                .interval(entity.getInterval())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .rowCount(entity.getRowCount())
                .checksum(entity.getChecksum())
                .sourceMetadata(readJsonObject(entity.getSourceMetadataJson()))
                .coverageMetadata(readJsonObject(entity.getCoverageMetadataJson()))
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private DatasetQualityReportResponse toQualityReportResponse(DatasetQualityReportEntity entity) {
        if (entity == null) {
            return null;
        }
        return DatasetQualityReportResponse.builder()
                .id(entity.getId())
                .datasetId(entity.getDatasetId())
                .datasetSnapshotId(entity.getDatasetSnapshotId())
                .qualityStatus(entity.getQualityStatus())
                .issues(readJsonObject(entity.getIssuesJson()))
                .checkedAt(entity.getCheckedAt())
                .build();
    }
}
