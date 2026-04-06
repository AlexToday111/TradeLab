package com.example.back.datasets.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.back.datasets.dto.RenameDatasetRequest;
import com.example.back.datasets.service.DatasetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class DatasetControllerTest {

    @Mock
    private DatasetService datasetService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DatasetController(datasetService)).build();
    }

    @Test
    void getDatasetsReturnsPayloadList() throws Exception {
        when(datasetService.getDatasets()).thenReturn(List.of(datasetNode("dataset-1", "Dataset 1")));

        mockMvc.perform(get("/api/datasets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is("dataset-1")));
    }

    @Test
    void createDatasetReturnsCreatedStatus() throws Exception {
        ObjectNode payload = datasetNode("dataset-1", "Dataset 1");
        when(datasetService.createDataset(payload)).thenReturn(payload);

        mockMvc.perform(post("/api/datasets").contentType("application/json").content(payload.toString()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name", is("Dataset 1")));
    }

    @Test
    void renameDatasetReturnsUpdatedPayload() throws Exception {
        ObjectNode renamed = datasetNode("dataset-1", "Renamed");
        when(datasetService.renameDataset("dataset-1", new RenameDatasetRequest("Renamed"))).thenReturn(renamed);

        mockMvc.perform(
                patch("/api/datasets/dataset-1")
                    .contentType("application/json")
                    .content("{\"name\":\"Renamed\"}")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Renamed")));
    }

    @Test
    void deleteDatasetReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/datasets/dataset-1")).andExpect(status().isNoContent());
    }

    private ObjectNode datasetNode(String id, String name) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("id", id);
        node.put("name", name);
        return node;
    }
}
