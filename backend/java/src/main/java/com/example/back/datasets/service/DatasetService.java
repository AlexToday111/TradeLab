package com.example.back.datasets.service;

import com.example.back.auth.security.AuthContext;
import com.example.back.datasets.dto.DatasetDetailsResponse;
import com.example.back.datasets.dto.DatasetQualityReportResponse;
import com.example.back.datasets.dto.DatasetSnapshotResponse;
import com.example.back.datasets.dto.RenameDatasetRequest;
import com.example.back.datasets.entity.DatasetEntity;
import com.example.back.datasets.entity.DatasetQualityReportEntity;
import com.example.back.datasets.entity.DatasetQualityReportEntity.QualityStatus;
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
        persistSnapshotAndQuality(saved, normalizedPayload);
        return readPayload(saved);
    }

    @Transactional(readOnly = true)
    public DatasetDetailsResponse getDatasetDetails(String id) {
        Long userId = AuthContext.requireUserId();
        DatasetEntity entity = datasetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found"));

        return DatasetDetailsResponse.builder()
                .dataset(readPayload(entity))
                .latestSnapshot(datasetSnapshotRepository
                        .findFirstByDatasetIdAndUserIdOrderByCreatedAtDesc(id, userId)
                        .map(this::toSnapshotResponse)
                        .orElse(null))
                .latestQualityReport(datasetQualityReportRepository
                        .findFirstByDatasetIdAndUserIdOrderByCheckedAtDesc(id, userId)
                        .map(this::toQualityReportResponse)
                        .orElse(null))
                .build();
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

        DatasetEntity saved = datasetRepository.save(duplicate);
        persistSnapshotAndQuality(saved, payload);
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
        persistSnapshotAndQuality(saved, payload);
        return readPayload(saved);
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

    @Transactional(readOnly = true)
    public List<DatasetSnapshotResponse> getDatasetVersions(String id) {
        Long userId = AuthContext.requireUserId();
        requireOwnedDataset(id, userId);
        return datasetSnapshotRepository.findAllByDatasetIdAndUserIdOrderByCreatedAtDesc(id, userId).stream()
                .map(this::toSnapshotResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DatasetQualityReportResponse> getDatasetQualityReports(String id) {
        Long userId = AuthContext.requireUserId();
        requireOwnedDataset(id, userId);
        return datasetQualityReportRepository.findAllByDatasetIdAndUserIdOrderByCheckedAtDesc(id, userId).stream()
                .map(this::toQualityReportResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DatasetSnapshotResponse getDatasetSnapshot(Long snapshotId) {
        Long userId = AuthContext.requireUserId();
        return datasetSnapshotRepository.findByIdAndUserId(snapshotId, userId)
                .map(this::toSnapshotResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset snapshot not found"));
    }

    @Transactional(readOnly = true)
    public Optional<DatasetSnapshotEntity> findLatestSnapshotForDataset(String datasetId, Long userId) {
        return datasetSnapshotRepository.findFirstByDatasetIdAndUserIdOrderByCreatedAtDesc(datasetId, userId);
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

    private void persistSnapshotAndQuality(DatasetEntity entity, ObjectNode payload) {
        DatasetSnapshotEntity snapshot = new DatasetSnapshotEntity();
        snapshot.setDatasetId(entity.getId());
        snapshot.setUserId(entity.getUserId());
        snapshot.setDatasetVersion(firstNonBlank(entity.getVersion(), entity.getFingerprint(), entity.getId()));
        snapshot.setSourceExchange(entity.getSource());
        snapshot.setSymbol(entity.getSymbol());
        snapshot.setTimeframe(entity.getInterval());
        snapshot.setStartTime(entity.getStartAt());
        snapshot.setEndTime(entity.getEndAt());
        snapshot.setRowCount(entity.getRowsCount());
        snapshot.setChecksum(firstNonBlank(entity.getFingerprint(), entity.getVersion(), null));
        snapshot.setSourceMetadataJson(writePayload(sourceMetadata(payload)));
        snapshot.setCoverageMetadataJson(writePayload(coverageMetadata(payload, entity)));
        DatasetSnapshotEntity savedSnapshot = datasetSnapshotRepository.save(snapshot);

        DatasetQualityReportEntity qualityReport = new DatasetQualityReportEntity();
        qualityReport.setDatasetId(entity.getId());
        qualityReport.setSnapshotId(savedSnapshot.getId());
        qualityReport.setUserId(entity.getUserId());
        qualityReport.setQualityStatus(resolveQualityStatus(payload));
        qualityReport.setIssuesJson(writePayload(resolveQualityIssues(payload)));
        qualityReport.setCheckedAt(resolveCheckedAt(payload).orElse(Instant.now()));
        datasetQualityReportRepository.save(qualityReport);
    }

    private ObjectNode sourceMetadata(ObjectNode payload) {
        ObjectNode source = objectMapper.createObjectNode();
        source.put("sourceExchange", payload.path("source").asText(null));
        source.set("lineage", payload.path("lineage").isMissingNode()
                ? objectMapper.createObjectNode()
                : payload.path("lineage"));
        source.set("raw", payload.path("raw").isMissingNode()
                ? objectMapper.createObjectNode()
                : payload.path("raw"));
        return source;
    }

    private ObjectNode coverageMetadata(ObjectNode payload, DatasetEntity entity) {
        ObjectNode coverage = objectMapper.createObjectNode();
        coverage.put("startTime", entity.getStartAt() == null ? null : entity.getStartAt().toString());
        coverage.put("endTime", entity.getEndAt() == null ? null : entity.getEndAt().toString());
        coverage.put("rowCount", entity.getRowsCount());
        coverage.put("timeframe", entity.getInterval());
        coverage.set("coverage", payload.path("coverage").isMissingNode()
                ? objectMapper.createObjectNode()
                : payload.path("coverage"));
        return coverage;
    }

    private QualityStatus resolveQualityStatus(ObjectNode payload) {
        String status = payload.path("qualityStatus").asText("").trim();
        if (!status.isEmpty()) {
            try {
                return QualityStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                return QualityStatus.WARNING;
            }
        }

        JsonNode issues = resolveQualityIssues(payload);
        if (issues.isArray() && issues.size() == 0) {
            return QualityStatus.OK;
        }
        if (issues.isArray() && hasFatalIssue(issues)) {
            return QualityStatus.FAILED;
        }
        return QualityStatus.WARNING;
    }

    private boolean hasFatalIssue(JsonNode issues) {
        for (JsonNode issue : issues) {
            String severity = issue.path("severity").asText("").trim();
            if ("FAILED".equalsIgnoreCase(severity) || "ERROR".equalsIgnoreCase(severity)) {
                return true;
            }
            String code = issue.path("code").asText("").trim();
            if ("empty_dataset".equals(code)) {
                return true;
            }
        }
        return false;
    }

    private JsonNode resolveQualityIssues(ObjectNode payload) {
        JsonNode reportIssues = payload.path("qualityReport").path("issues");
        if (reportIssues.isArray()) {
            return reportIssues;
        }

        JsonNode flags = payload.path("qualityFlags");
        if (!flags.isArray()) {
            return objectMapper.createArrayNode();
        }

        return flags;
    }

    private Optional<Instant> resolveCheckedAt(ObjectNode payload) {
        String checkedAt = payload.path("qualityReport").path("checkedAt").asText("").trim();
        if (checkedAt.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Instant.parse(checkedAt));
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

    private void requireOwnedDataset(String id, Long userId) {
        if (!datasetRepository.existsByIdAndUserId(id, userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found");
        }
    }

    private DatasetSnapshotResponse toSnapshotResponse(DatasetSnapshotEntity entity) {
        return DatasetSnapshotResponse.builder()
                .id(entity.getId())
                .datasetId(entity.getDatasetId())
                .datasetVersion(entity.getDatasetVersion())
                .sourceExchange(entity.getSourceExchange())
                .symbol(entity.getSymbol())
                .timeframe(entity.getTimeframe())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .rowCount(entity.getRowCount())
                .checksum(entity.getChecksum())
                .sourceMetadata(readJson(entity.getSourceMetadataJson()))
                .coverageMetadata(readJson(entity.getCoverageMetadataJson()))
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private DatasetQualityReportResponse toQualityReportResponse(DatasetQualityReportEntity entity) {
        return DatasetQualityReportResponse.builder()
                .id(entity.getId())
                .datasetId(entity.getDatasetId())
                .snapshotId(entity.getSnapshotId())
                .qualityStatus(entity.getQualityStatus())
                .issues(readJson(entity.getIssuesJson()))
                .checkedAt(entity.getCheckedAt())
                .build();
    }

    private String firstNonBlank(String first, String second, String fallback) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return fallback;
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
        return readJson(entity.getPayload());
    }

    private JsonNode readJson(String json) {
        try {
            if (json == null || json.isBlank()) {
                return objectMapper.createObjectNode();
            }
            return objectMapper.readTree(json);
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
