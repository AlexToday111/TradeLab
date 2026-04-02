package com.example.back.datasets.controller;

import com.example.back.datasets.dto.RenameDatasetRequest;
import com.example.back.datasets.service.DatasetService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

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
@Tag(
        name = "Datasets",
        description = "Операции управления датасетами"
)
public class DatasetController {

    private final DatasetService datasetService;

    public DatasetController(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    @Operation(
            summary = "Получить список датасетов",
            description = "Возвращает список всех доступных датасетов"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список датасетов успешно получен",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = Object.class))
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping
    public List<JsonNode> getDatasets() {
        return datasetService.getDatasets();
    }

    @Operation(
            summary = "Создать датасет",
            description = "Создаёт новый датасет по переданному JSON payload"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Датасет успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректный payload"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JsonNode createDataset(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "JSON payload для создания датасета",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Object.class))
            )
            @RequestBody JsonNode payload
    ) {
        return datasetService.createDataset(payload);
    }

    @Operation(
            summary = "Переименовать датасет",
            description = "Изменяет имя существующего датасета по его идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Датасет успешно переименован"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Датасет не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PatchMapping("/{id}")
    public JsonNode renameDataset(
            @Parameter(description = "Идентификатор датасета", example = "dataset-001")
            @PathVariable String id,

            @RequestBody RenameDatasetRequest request
    ) {
        return datasetService.renameDataset(id, request);
    }

    @Operation(
            summary = "Дублировать датасет",
            description = "Создаёт копию существующего датасета"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Датасет успешно продублирован"),
            @ApiResponse(responseCode = "404", description = "Исходный датасет не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/{id}/duplicate")
    public JsonNode duplicateDataset(
            @Parameter(description = "Идентификатор датасета", example = "dataset-001")
            @PathVariable String id
    ) {
        return datasetService.duplicateDataset(id);
    }

    @Operation(
            summary = "Удалить датасет",
            description = "Удаляет датасет по его идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Датасет успешно удалён"),
            @ApiResponse(responseCode = "404", description = "Датасет не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDataset(
            @Parameter(description = "Идентификатор датасета", example = "dataset-001")
            @PathVariable String id
    ) {
        datasetService.deleteDataset(id);
    }
}