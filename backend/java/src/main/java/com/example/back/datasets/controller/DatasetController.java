package com.example.back.datasets.controller;

import com.example.back.datasets.dto.RenameDatasetRequest;
import com.example.back.datasets.service.DatasetService;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasets")
public class DatasetController {

    private final DatasetService datasetService;

    public DatasetController(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    @GetMapping
    public List<JsonNode> getDatasets() {
        return datasetService.getDatasets();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JsonNode createDataset(@RequestBody JsonNode payload) {
        return datasetService.createDataset(payload);
    }

    @PatchMapping("/{id}")
    public JsonNode renameDataset(@PathVariable String id, @RequestBody RenameDatasetRequest request) {
        return datasetService.renameDataset(id, request);
    }

    @PostMapping("/{id}/duplicate")
    public JsonNode duplicateDataset(@PathVariable String id) {
        return datasetService.duplicateDataset(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDataset(@PathVariable String id) {
        datasetService.deleteDataset(id);
    }
}
