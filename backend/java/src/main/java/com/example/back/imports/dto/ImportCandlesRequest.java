package com.example.back.imports.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ImportCandlesRequest {

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
}
