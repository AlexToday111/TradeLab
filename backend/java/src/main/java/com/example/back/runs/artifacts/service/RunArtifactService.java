package com.example.back.runs.artifacts.service;

import com.example.back.auth.security.AuthContext;
import com.example.back.backtest.dto.BacktestTrade;
import com.example.back.backtest.dto.EquityPoint;
import com.example.back.backtest.exception.BacktestResourceNotFoundException;
import com.example.back.runs.artifacts.dto.RunArtifactContentResponse;
import com.example.back.runs.artifacts.dto.RunArtifactResponse;
import com.example.back.runs.artifacts.entity.RunArtifactEntity;
import com.example.back.runs.artifacts.entity.RunArtifactEntity.ArtifactType;
import com.example.back.runs.artifacts.repository.RunArtifactRepository;
import com.example.back.runs.entity.RunEntity;
import com.example.back.runs.repository.RunRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RunArtifactService {

    private static final String JSON_CONTENT_TYPE = "application/json";

    private final RunRepository runRepository;
    private final RunArtifactRepository runArtifactRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void replaceArtifacts(
            RunEntity run,
            Map<String, Object> summary,
            Map<String, Object> metrics,
            List<BacktestTrade> trades,
            List<EquityPoint> equityCurve,
            Map<String, Object> engineArtifacts
    ) {
        runArtifactRepository.deleteByRunId(run.getId());
        runArtifactRepository.saveAll(List.of(
                buildArtifact(run.getId(), ArtifactType.EQUITY_CURVE, "equity_curve.json", equityCurve),
                buildArtifact(run.getId(), ArtifactType.TRADES, "trades.json", trades),
                buildArtifact(run.getId(), ArtifactType.METRICS_JSON, "metrics.json", metrics),
                buildArtifact(run.getId(), ArtifactType.SUMMARY_JSON, "summary.json", summary),
                buildArtifact(run.getId(), ArtifactType.REPORT_JSON, "run_report.json",
                        buildRunReport(run, summary, metrics, trades, equityCurve, engineArtifacts))
        ));
    }

    @Transactional
    public void clearArtifacts(Long runId) {
        runArtifactRepository.deleteByRunId(runId);
    }

    @Transactional(readOnly = true)
    public List<RunArtifactResponse> listArtifacts(Long runId) {
        requireOwnedRun(runId);
        return runArtifactRepository.findAllByRunIdOrderByCreatedAtAsc(runId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RunArtifactContentResponse getArtifact(Long runId, Long artifactId) {
        requireOwnedRun(runId);
        RunArtifactEntity artifact = runArtifactRepository.findByIdAndRunId(artifactId, runId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artifact not found"));

        return RunArtifactContentResponse.builder()
                .id(artifact.getId())
                .runId(artifact.getRunId())
                .artifactType(artifact.getArtifactType())
                .artifactName(artifact.getArtifactName())
                .contentType(artifact.getContentType())
                .storagePath(artifact.getStoragePath())
                .sizeBytes(artifact.getSizeBytes())
                .createdAt(artifact.getCreatedAt())
                .payload(readJson(artifact.getPayloadJson()))
                .build();
    }

    @Transactional(readOnly = true)
    public RunArtifactEntity getArtifactEntity(Long runId, Long artifactId) {
        requireOwnedRun(runId);
        return runArtifactRepository.findByIdAndRunId(artifactId, runId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artifact not found"));
    }

    public JsonNode readArtifactPayload(RunArtifactEntity artifact) {
        return readJson(artifact.getPayloadJson());
    }

    private RunEntity requireOwnedRun(Long runId) {
        Long userId = AuthContext.requireUserId();
        return runRepository.findByIdAndUserId(runId, userId)
                .orElseThrow(() -> new BacktestResourceNotFoundException("Run not found: " + runId));
    }

    private RunArtifactEntity buildArtifact(
            Long runId,
            ArtifactType artifactType,
            String artifactName,
            Object payload
    ) {
        String payloadJson = writeJson(payload);
        RunArtifactEntity artifact = new RunArtifactEntity();
        artifact.setRunId(runId);
        artifact.setArtifactType(artifactType);
        artifact.setArtifactName(artifactName);
        artifact.setContentType(JSON_CONTENT_TYPE);
        artifact.setStoragePath("db://run_artifacts/" + runId + "/" + artifactName);
        artifact.setPayloadJson(payloadJson);
        artifact.setSizeBytes((long) payloadJson.getBytes(StandardCharsets.UTF_8).length);
        return artifact;
    }

    private Map<String, Object> buildRunReport(
            RunEntity run,
            Map<String, Object> summary,
            Map<String, Object> metrics,
            List<BacktestTrade> trades,
            List<EquityPoint> equityCurve,
            Map<String, Object> engineArtifacts
    ) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("runId", run.getId());
        report.put("correlationId", run.getCorrelationId());
        report.put("strategyId", run.getStrategyId());
        report.put("strategyName", run.getStrategyName());
        report.put("datasetId", run.getDatasetId());
        report.put("exchange", run.getExchange());
        report.put("symbol", run.getSymbol());
        report.put("interval", run.getInterval());
        report.put("from", run.getDateFrom());
        report.put("to", run.getDateTo());
        report.put("engineVersion", run.getEngineVersion());
        report.put("summary", summary);
        report.put("metrics", metrics);
        report.put("tradesCount", trades.size());
        report.put("equityPointCount", equityCurve.size());
        report.put("engineArtifacts", engineArtifacts);
        return report;
    }

    private RunArtifactResponse toResponse(RunArtifactEntity artifact) {
        return RunArtifactResponse.builder()
                .id(artifact.getId())
                .runId(artifact.getRunId())
                .artifactType(artifact.getArtifactType())
                .artifactName(artifact.getArtifactName())
                .contentType(artifact.getContentType())
                .storagePath(artifact.getStoragePath())
                .sizeBytes(artifact.getSizeBytes())
                .createdAt(artifact.getCreatedAt())
                .build();
    }

    private JsonNode readJson(String json) {
        try {
            if (json == null || json.isBlank()) {
                return objectMapper.createObjectNode();
            }
            return objectMapper.readTree(json);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse artifact payload", exception);
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize artifact payload", exception);
        }
    }
}
