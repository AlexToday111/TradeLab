package com.example.back.strategies.controller;

import com.example.back.strategies.dto.CreateStrategyPresetRequest;
import com.example.back.strategies.dto.CreateStrategyRequest;
import com.example.back.strategies.dto.StrategyPresetResponse;
import com.example.back.strategies.dto.StrategyResponse;
import com.example.back.strategies.dto.StrategyVersionResponse;
import com.example.back.strategies.dto.UpdateStrategyPresetRequest;
import com.example.back.strategies.dto.UpdateStrategyRequest;
import com.example.back.strategies.service.StrategyFileService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StrategyFileController {

    private final StrategyFileService strategyFileService;

    @PostMapping("/strategies")
    @ResponseStatus(HttpStatus.CREATED)
    public StrategyResponse createStrategy(@Valid @RequestBody CreateStrategyRequest request) {
        return strategyFileService.createStrategy(request);
    }

    @PostMapping(value = "/strategies/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StrategyResponse> uploadStrategy(@RequestParam("file") MultipartFile file) {
        StrategyResponse response = strategyFileService.uploadStrategy(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/strategies")
    public List<StrategyResponse> getAllStrategies() {
        return strategyFileService.getAllStrategies();
    }

    @GetMapping("/strategies/{id}")
    public StrategyResponse getStrategyById(@PathVariable Long id) {
        return strategyFileService.getStrategyById(id);
    }

    @PatchMapping("/strategies/{id}")
    public StrategyResponse updateStrategy(
            @PathVariable Long id,
            @RequestBody UpdateStrategyRequest request
    ) {
        return strategyFileService.updateStrategy(id, request);
    }

    @PostMapping("/strategies/{id}/archive")
    public StrategyResponse archiveStrategy(@PathVariable Long id) {
        return strategyFileService.archiveStrategy(id);
    }

    @PostMapping(value = "/strategies/{id}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public StrategyVersionResponse createStrategyVersion(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        return strategyFileService.createStrategyVersion(id, file);
    }

    @GetMapping("/strategies/{id}/versions")
    public List<StrategyVersionResponse> getStrategyVersions(@PathVariable Long id) {
        return strategyFileService.getStrategyVersions(id);
    }

    @GetMapping("/strategy-versions/{versionId}")
    public StrategyVersionResponse getStrategyVersion(@PathVariable Long versionId) {
        return strategyFileService.getStrategyVersion(versionId);
    }

    @PostMapping("/strategy-versions/{versionId}/validate")
    public StrategyVersionResponse validateStrategyVersion(@PathVariable Long versionId) {
        return strategyFileService.validateStrategyVersion(versionId);
    }

    @PostMapping("/strategy-versions/{versionId}/activate")
    public StrategyVersionResponse activateStrategyVersion(@PathVariable Long versionId) {
        return strategyFileService.activateStrategyVersion(versionId);
    }

    @PostMapping("/strategies/{id}/presets")
    @ResponseStatus(HttpStatus.CREATED)
    public StrategyPresetResponse createPreset(
            @PathVariable Long id,
            @Valid @RequestBody CreateStrategyPresetRequest request
    ) {
        return strategyFileService.createPreset(id, request);
    }

    @GetMapping("/strategies/{id}/presets")
    public List<StrategyPresetResponse> getPresets(@PathVariable Long id) {
        return strategyFileService.getPresets(id);
    }

    @PatchMapping("/strategy-presets/{presetId}")
    public StrategyPresetResponse updatePreset(
            @PathVariable Long presetId,
            @RequestBody UpdateStrategyPresetRequest request
    ) {
        return strategyFileService.updatePreset(presetId, request);
    }

    @DeleteMapping("/strategy-presets/{presetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePreset(@PathVariable Long presetId) {
        strategyFileService.deletePreset(presetId);
    }
}
