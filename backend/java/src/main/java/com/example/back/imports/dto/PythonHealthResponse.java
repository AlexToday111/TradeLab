package com.example.back.imports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Ответ от Python parser сервиса")
public class PythonHealthResponse {

    @Schema(
            description = "Статус сервиса",
            example = "ok"
    )
    private String status;

    @Schema(
            description = "Название сервиса",
            example = "python-parser"
    )
    private String service;
}