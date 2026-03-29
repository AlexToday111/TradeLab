package com.example.back.strategies.dto;

import com.example.back.strategies.entity.StrategyFileEntity;

import java.util.List;

public record StrategyUploadResponse (
        Long id,
        String name,
        String fileName,
        StrategyFileEntity.StrategyStatus status,
        String validationError,
        List<String> parametersSchema
){
}
