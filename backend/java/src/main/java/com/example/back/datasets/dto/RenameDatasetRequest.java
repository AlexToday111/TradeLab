package com.example.back.datasets.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на переименование датасета")
public record RenameDatasetRequest(

        @Schema(description = "Новое имя датасета", example = "btcusdt_1h_2024_copy")
        String name

) {}