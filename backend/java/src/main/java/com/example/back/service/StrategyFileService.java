package com.example.back.service;

import com.example.back.client.PythonParserClient;
import com.example.back.dto.StrategyResponse;
import com.example.back.dto.StrategyValidationRequest;
import com.example.back.dto.StrategyValidationResponse;
import com.example.back.entity.StrategyFileEntity;
import com.example.back.repository.StrategyFileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyFileService {

    private final StrategyFileRepository strategyFileRepository;
    private final PythonParserClient pythonParserClient;
    private final ObjectMapper objectMapper;

    @Value("${app.strategy-storage-path:./storage/strategies}")
    private String storagePath;

    public StrategyResponse uploadStrategy(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.endsWith(".py")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must have .py extension");
            }

            String fileName = generateUniqueFileName(originalFilename);
            Path filePath = Paths.get(storagePath, fileName);

            Files.createDirectories(Paths.get(storagePath));

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File saved to: {}", filePath);

            StrategyFileEntity entity = new StrategyFileEntity();
            entity.setName(null);
            entity.setFileName(fileName);
            entity.setStoragePath(filePath.toAbsolutePath().toString());
            entity.setStatus(StrategyFileEntity.StrategyStatus.PENDING);
            entity.setValidationError(null);
            entity.setParametersSchemaJson(null);
            entity.setCreatedAt(Instant.now());

            StrategyFileEntity savedEntity = strategyFileRepository.save(entity);
            log.info("Created strategy record with id: {}", savedEntity.getId());

            StrategyValidationRequest validationRequest = new StrategyValidationRequest();
            validationRequest.setFilePath(filePath.toAbsolutePath().toString());
            applyValidationResult(savedEntity, validateWithPython(validationRequest));

            StrategyFileEntity updatedEntity = strategyFileRepository.save(savedEntity);

            return StrategyResponse.fromEntity(updatedEntity);

        } catch (IOException e) {
            log.error("Error while saving file", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save file", e);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during strategy upload", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process strategy", e);
        }
    }

    public List<StrategyResponse> getAllStrategies() {
        return strategyFileRepository.findAll().stream()
                .map(StrategyResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public StrategyResponse getStrategyById(Long id) {
        StrategyFileEntity entity = strategyFileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Strategy not found with id: " + id));
        return StrategyResponse.fromEntity(entity);
    }

    private String generateUniqueFileName(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return timestamp + "_" + originalFilename;
    }

    private StrategyValidationResponse validateWithPython(StrategyValidationRequest request) {
        try {
            StrategyValidationResponse response = pythonParserClient.validateStrategy(request);
            log.info("Python validation response: {}", response);
            if (response == null) {
                return new StrategyValidationResponse(false, null, null, "Python validation returned empty response");
            }
            return response;
        } catch (RestClientException exception) {
            log.error("Python validation request failed for {}", request.getFilePath(), exception);
            return new StrategyValidationResponse(false, null, null, "Python validation request failed");
        }
    }

    private void applyValidationResult(StrategyFileEntity entity, StrategyValidationResponse validationResponse)
            throws IOException {
        if (Boolean.TRUE.equals(validationResponse.getValid())) {
            entity.setStatus(StrategyFileEntity.StrategyStatus.VALID);
            entity.setName(validationResponse.getName());
            entity.setValidationError(null);
            entity.setParametersSchemaJson(objectMapper.writeValueAsString(validationResponse.getParametersSchema()));
            log.info("Strategy validated successfully: {}", entity.getId());
            return;
        }

        entity.setStatus(StrategyFileEntity.StrategyStatus.INVALID);
        entity.setName(null);
        entity.setValidationError(validationResponse.getError());
        entity.setParametersSchemaJson(null);
        log.warn("Strategy validation failed: {} - {}", entity.getId(), validationResponse.getError());
    }
}
