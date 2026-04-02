package com.example.back.runs.controller;

import com.example.back.runs.dto.CreateRunRequest;
import com.example.back.runs.dto.RunResponse;
import com.example.back.runs.service.RunService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/runs")
@Tag(
        name = "Runs",
        description = "Операции управления запусками стратегий и бэктестов"
)
public class RunController {

    private final RunService runService;

    public RunController(RunService runService) {
        this.runService = runService;
    }

    @Operation(
            summary = "Создать запуск",
            description = "Создаёт новый запуск стратегии или бэктеста на основе переданных параметров"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Запуск успешно создан",
                    content = @Content(schema = @Schema(implementation = RunResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные параметры запроса"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Связанные сущности не найдены"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RunResponse createRun(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Параметры для создания запуска",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateRunRequest.class))
            )
            @RequestBody CreateRunRequest request
    ) {
        return runService.createRun(request);
    }

    @Operation(
            summary = "Получить список запусков",
            description = "Возвращает список всех запусков"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список запусков успешно получен",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = RunResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<RunResponse> getRuns() {
        return runService.getRuns();
    }

    @Operation(
            summary = "Получить запуск по ID",
            description = "Возвращает информацию о запуске по его идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Запуск найден",
                    content = @Content(schema = @Schema(implementation = RunResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Запуск не найден"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RunResponse getRunById(
            @Parameter(
                    description = "Идентификатор запуска",
                    example = "1",
                    required = true
            )
            @PathVariable Long id
    ) {
        return runService.getRunById(id);
    }
}