package com.example.back.strategies.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StrategyValidationRequest {
    @NotBlank(message = "Путь к файлу не может быть пустым")
    private String filePath;
}
