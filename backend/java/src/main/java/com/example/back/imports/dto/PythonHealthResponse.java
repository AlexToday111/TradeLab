package com.example.back.imports.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PythonHealthResponse {

    @NotBlank(message = "Статус не может быть пустым")
    private String status;

    @NotBlank(message = "Название сервиса не может быть пустым")
    private String service;
}
