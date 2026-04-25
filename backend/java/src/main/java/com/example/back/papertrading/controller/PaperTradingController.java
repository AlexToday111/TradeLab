package com.example.back.papertrading.controller;

import com.example.back.papertrading.dto.CreatePaperOrderRequest;
import com.example.back.papertrading.dto.CreatePaperSessionRequest;
import com.example.back.papertrading.dto.PaperFillResponse;
import com.example.back.papertrading.dto.PaperOrderResponse;
import com.example.back.papertrading.dto.PaperPositionResponse;
import com.example.back.papertrading.dto.PaperSessionResponse;
import com.example.back.papertrading.dto.PaperSummaryResponse;
import com.example.back.papertrading.service.PaperTradingService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/paper")
@RequiredArgsConstructor
public class PaperTradingController {

    private final PaperTradingService paperTradingService;

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public PaperSessionResponse createSession(@Valid @RequestBody CreatePaperSessionRequest request) {
        return paperTradingService.createSession(request);
    }

    @GetMapping("/sessions")
    public List<PaperSessionResponse> listSessions() {
        return paperTradingService.listSessions();
    }

    @GetMapping("/sessions/{id}")
    public PaperSessionResponse getSession(@PathVariable Long id) {
        return paperTradingService.getSession(id);
    }

    @PostMapping("/sessions/{id}/start")
    public PaperSessionResponse startSession(@PathVariable Long id) {
        return paperTradingService.startSession(id);
    }

    @PostMapping("/sessions/{id}/pause")
    public PaperSessionResponse pauseSession(@PathVariable Long id) {
        return paperTradingService.pauseSession(id);
    }

    @PostMapping("/sessions/{id}/stop")
    public PaperSessionResponse stopSession(@PathVariable Long id) {
        return paperTradingService.stopSession(id);
    }

    @PostMapping("/sessions/{id}/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public PaperOrderResponse placeOrder(
            @PathVariable Long id,
            @Valid @RequestBody CreatePaperOrderRequest request
    ) {
        return paperTradingService.placeOrder(id, request);
    }

    @GetMapping("/sessions/{id}/orders")
    public List<PaperOrderResponse> listOrders(@PathVariable Long id) {
        return paperTradingService.listOrders(id);
    }

    @GetMapping("/orders/{orderId}")
    public PaperOrderResponse getOrder(@PathVariable Long orderId) {
        return paperTradingService.getOrder(orderId);
    }

    @PostMapping("/orders/{orderId}/cancel")
    public PaperOrderResponse cancelOrder(@PathVariable Long orderId) {
        return paperTradingService.cancelOrder(orderId);
    }

    @GetMapping("/sessions/{id}/positions")
    public List<PaperPositionResponse> listPositions(@PathVariable Long id) {
        return paperTradingService.listPositions(id);
    }

    @GetMapping("/sessions/{id}/fills")
    public List<PaperFillResponse> listFills(@PathVariable Long id) {
        return paperTradingService.listFills(id);
    }

    @GetMapping("/sessions/{id}/summary")
    public PaperSummaryResponse getSummary(@PathVariable Long id) {
        return paperTradingService.getSummary(id);
    }
}
