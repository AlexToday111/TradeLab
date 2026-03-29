package com.example.back.strategies.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StrategyValidationResponse {
    @NotNull(message = "Поле valid обязательно")
    private Boolean valid;
    @NotBlank(message = "Название стратегии не может быть пустым")
    private String name;
    @NotNull(message = "Схема параметров обязательна")
    private Map<String, Object> parametersSchema;

    private String error;
}
