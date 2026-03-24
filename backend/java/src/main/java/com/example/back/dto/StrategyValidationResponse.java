package com.example.back.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StrategyValidationResponse {
    private Boolean valid;
    private String name;
    private Map<String, Object> parametersSchema;
    private String error;
}
