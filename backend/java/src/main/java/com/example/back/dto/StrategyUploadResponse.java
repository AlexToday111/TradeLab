package com.example.back.dto;

import com.example.back.entity.StrategyFileEntity;

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
