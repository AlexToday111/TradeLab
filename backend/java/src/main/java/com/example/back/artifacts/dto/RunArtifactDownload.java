package com.example.back.artifacts.dto;

public record RunArtifactDownload(
        String fileName,
        String contentType,
        byte[] content
) {
}
