package com.example.back.runs.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

@Data
@NoArgsConstructor
public class CreateRunRequest {

    @NotNull(message = "ID стратегии обязателен")
    private Long strategyId;

    @NotBlank(message = "Биржа не может быть пустой")
    private String exchange;

    @NotBlank(message = "Символ (торговая пара) не может быть пустым")
    private String symbol;

    @NotBlank(message = "Интервал свечей не может быть пустым")
    private String interval;

    @NotBlank(message = "Дата начала (from) не может быть пустой")
    private String from;

    @NotBlank(message = "Дата окончания (to) не может быть пустой")
    private String to;

    @NotNull(message = "Параметры стратегии обязательны")
    private Map<String, Object> params;
}
