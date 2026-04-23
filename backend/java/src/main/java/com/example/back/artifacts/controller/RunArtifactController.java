package com.example.back.artifacts.controller;

import com.example.back.artifacts.dto.RunArtifactContentResponse;
import com.example.back.artifacts.dto.RunArtifactDownload;
import com.example.back.artifacts.dto.RunArtifactMetadataResponse;
import com.example.back.artifacts.service.RunArtifactService;
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

    @GetMapping
    public List<RunArtifactMetadataResponse> listArtifacts(@PathVariable Long runId) {
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
    ) {
        RunArtifactDownload download = runArtifactService.downloadArtifact(runId, artifactId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(download.fileName()).build().toString()
                )
                .body(download.content());
    }
}
