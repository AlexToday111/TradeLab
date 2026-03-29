package com.example.back.runs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RunResponse {

    @NotNull(message = "ID запуска не может быть пустым")
    private Long id;

    @NotNull(message = "ID стратегии не может быть пустым")
    private Long strategyId;

    @NotBlank(message = "Статус не может быть пустым")
    private String status;

    @NotBlank(message = "Биржа не может быть пустой")
    private String exchange;

    @NotBlank(message = "Символ не может быть пустым")
    private String symbol;

    @NotBlank(message = "Интервал не может быть пустым")
    private String interval;

    @NotBlank(message = "Дата начала не может быть пустой")
    private String from;

    @NotBlank(message = "Дата окончания не может быть пустой")
    private String to;

    @NotNull(message = "Параметры не могут быть пустыми")
    private Map<String, Object> params;

    private Map<String, Object> metrics;

    private String errorMessage;

    @NotNull(message = "Дата создания не может быть пустой")
    private Instant createdAt;

    private Instant finishedAt;
}
