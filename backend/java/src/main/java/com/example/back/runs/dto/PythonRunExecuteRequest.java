package com.example.back.runs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PythonRunExecuteRequest {

    @NotBlank(message = "Путь к файлу стратегии не может быть пустым")
    private String strategyFilePath;

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

    private String runId;

    private String jobId;

    private String correlationId;
}
