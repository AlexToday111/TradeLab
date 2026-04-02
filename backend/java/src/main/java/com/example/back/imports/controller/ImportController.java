package com.example.back.imports.controller;

import com.example.back.imports.dto.ImportCandlesRequest;
import com.example.back.imports.dto.ImportCandlesResponse;
import com.example.back.imports.service.ImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/imports")
@Tag(
        name = "Imports",
        description = "Операции импорта рыночных данных через Python parser"
)
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @Operation(
            summary = "Импортировать свечи",
            description = "Запускает импорт исторических свечей по указанным параметрам через Python сервис"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Импорт успешно выполнен",
                    content = @Content(schema = @Schema(implementation = ImportCandlesResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные параметры запроса"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Python parser сервис недоступен"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    @PostMapping("/candles")
    public ImportCandlesResponse importCandles(
            @RequestBody(
                    description = "Параметры импорта свечей",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ImportCandlesRequest.class))
            )
            @org.springframework.web.bind.annotation.RequestBody ImportCandlesRequest request
    ) {
        return importService.importCandles(request);
    }
}