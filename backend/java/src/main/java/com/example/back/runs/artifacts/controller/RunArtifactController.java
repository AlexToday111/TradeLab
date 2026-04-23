package com.example.back.runs.artifacts.controller;

import com.example.back.runs.artifacts.dto.RunArtifactContentResponse;
import com.example.back.runs.artifacts.dto.RunArtifactResponse;
import com.example.back.runs.artifacts.entity.RunArtifactEntity;
import com.example.back.runs.artifacts.service.RunArtifactService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/runs/{runId}/artifacts")
@RequiredArgsConstructor
public class RunArtifactController {

    private final RunArtifactService runArtifactService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public List<RunArtifactResponse> listArtifacts(@PathVariable Long runId) {
        return runArtifactService.listArtifacts(runId);
    }

    @GetMapping("/{artifactId}")
    public RunArtifactContentResponse getArtifact(
            @PathVariable Long runId,
            @PathVariable Long artifactId
    ) {
        return runArtifactService.getArtifact(runId, artifactId);
    }

    @GetMapping("/{artifactId}/download")
    public ResponseEntity<byte[]> downloadArtifact(
            @PathVariable Long runId,
            @PathVariable Long artifactId
    ) throws JsonProcessingException {
        RunArtifactEntity artifact = runArtifactService.getArtifactEntity(runId, artifactId);
        byte[] payload = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(runArtifactService.readArtifactPayload(artifact))
                .getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(artifact.getContentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(artifact.getArtifactName(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .body(payload);
    }
}
