package com.example.back.strategies.controller;

import com.example.back.strategies.dto.StrategyTemplateResponse;
import com.example.back.strategies.service.StrategyTemplateService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/strategy-templates")
@RequiredArgsConstructor
public class StrategyTemplateController {

    private final StrategyTemplateService strategyTemplateService;

    @GetMapping
    public List<StrategyTemplateResponse> listTemplates() {
        return strategyTemplateService.listTemplates();
    }

    @GetMapping("/{id}")
    public StrategyTemplateResponse getTemplate(@PathVariable Long id) {
        return strategyTemplateService.getTemplate(id);
    }
}
