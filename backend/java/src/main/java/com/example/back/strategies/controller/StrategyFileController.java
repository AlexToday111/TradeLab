package com.example.back.strategies.controller;

import com.example.back.strategies.dto.StrategyResponse;
import com.example.back.strategies.service.StrategyFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/strategies")
@RequiredArgsConstructor
@Tag(
        name = "Strategies",
        description = "Операции загрузки, валидации и получения торговых стратегий"
)
public class StrategyFileController {

    private final StrategyFileService strategyFileService;

    @Operation(
            summary = "Загрузить стратегию",
            description = "Загружает файл стратегии, сохраняет его и возвращает результат обработки"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Стратегия успешно загружена",
                    content = @Content(schema = @Schema(implementation = StrategyResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный файл или запрос"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StrategyResponse> uploadStrategy(
            @Parameter(
                    description = "Python-файл стратегии",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            )
            @RequestParam("file") MultipartFile file
    ) {
        StrategyResponse response = strategyFileService.uploadStrategy(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Получить список стратегий",
            description = "Возвращает список всех загруженных стратегий"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список стратегий успешно получен",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = StrategyResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    @GetMapping
    public ResponseEntity<List<StrategyResponse>> getAllStrategies() {
        List<StrategyResponse> strategies = strategyFileService.getAllStrategies();
        return ResponseEntity.ok(strategies);
    }

    @Operation(
            summary = "Получить стратегию по ID",
            description = "Возвращает информацию о стратегии по её идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Стратегия найдена",
                    content = @Content(schema = @Schema(implementation = StrategyResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Стратегия не найдена"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<StrategyResponse> getStrategyById(
            @Parameter(description = "ID стратегии", example = "1", required = true)
            @PathVariable Long id
    ) {
        StrategyResponse strategy = strategyFileService.getStrategyById(id);
        return ResponseEntity.ok(strategy);
    }
}
