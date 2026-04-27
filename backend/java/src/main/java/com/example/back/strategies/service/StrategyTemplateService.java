package com.example.back.strategies.service;

import com.example.back.strategies.dto.StrategyTemplateResponse;
import com.example.back.strategies.entity.StrategyTemplateEntity;
import com.example.back.strategies.repository.StrategyTemplateRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class StrategyTemplateService {

    private final StrategyTemplateRepository templateRepository;
    private final ObjectMapper objectMapper;

    @PostConstruct
    void seedTemplates() {
        if (templateRepository.count() > 0) {
            return;
        }
        templateRepository.saveAll(List.of(
                template(
                        "mean-reversion",
                        "Mean Reversion",
                        "Signals when price diverges from a moving average.",
                        "MEAN_REVERSION",
                        Map.of("lookback", 20, "entryZScore", 2.0, "exitZScore", 0.5)
                ),
                template(
                        "momentum",
                        "Momentum",
                        "Follows recent directional strength with configurable windows.",
                        "MOMENTUM",
                        Map.of("fastWindow", 12, "slowWindow", 26, "threshold", 0.0)
                ),
                template(
                        "breakout",
                        "Breakout",
                        "Trades range breaks using a high-low channel.",
                        "BREAKOUT",
                        Map.of("channelWindow", 20, "riskFraction", 0.01)
                ),
                template(
                        "trend-following",
                        "Trend Following",
                        "Uses trend filters to enter aligned long or short positions.",
                        "TREND_FOLLOWING",
                        Map.of("trendWindow", 50, "confirmationWindow", 10)
                )
        ));
    }

    public List<StrategyTemplateResponse> listTemplates() {
        return templateRepository.findAllByOrderByNameAsc().stream()
                .map(StrategyTemplateResponse::fromEntity)
                .toList();
    }

    public StrategyTemplateResponse getTemplate(Long id) {
        return templateRepository.findById(id)
                .map(StrategyTemplateResponse::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Strategy template not found: " + id));
    }

    private StrategyTemplateEntity template(
            String key,
            String name,
            String description,
            String category,
            Map<String, Object> defaultParameters
    ) {
        StrategyTemplateEntity entity = new StrategyTemplateEntity();
        entity.setTemplateKey(key);
        entity.setName(name);
        entity.setDescription(description);
        entity.setStrategyType("BACKTEST");
        entity.setCategory(category);
        entity.setDefaultParametersJson(writeJson(defaultParameters));
        entity.setTemplateReference("system://" + key);
        entity.setMetadataJson(writeJson(Map.of(
                "expectedInputs", List.of("candles", "params"),
                "notes", "Starter template metadata only; source generation is handled by clients."
        )));
        return entity;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize strategy template JSON", exception);
        }
    }
}
