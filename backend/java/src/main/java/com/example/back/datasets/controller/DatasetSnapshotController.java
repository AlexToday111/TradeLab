package com.example.back.datasets.controller;

import com.example.back.datasets.dto.DatasetSnapshotResponse;
import com.example.back.datasets.service.DatasetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dataset-snapshots")
@RequiredArgsConstructor
public class DatasetSnapshotController {

    private final DatasetService datasetService;

    @GetMapping("/{snapshotId}")
    public DatasetSnapshotResponse getDatasetSnapshot(@PathVariable Long snapshotId) {
        return datasetService.getDatasetSnapshot(snapshotId);
    }
}
