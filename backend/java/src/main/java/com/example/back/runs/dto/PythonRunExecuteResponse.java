package com.example.back.runs.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PythonRunExecuteResponse {

    @NotNull(message = "Поле success обязательно")
    private Boolean success;

    private Map<String, Object> metrics;

    private String error;
}
