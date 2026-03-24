package com.example.back.controller;

import com.example.back.dto.CandleResponse;
import com.example.back.service.CandleQueryService;
import java.time.Instant;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/candles")
public class CandleController {

    private final CandleQueryService candleQueryService;

    public CandleController(CandleQueryService candleQueryService) {
        this.candleQueryService = candleQueryService;
    }

    @GetMapping
    public List<CandleResponse> getCandles(
        @RequestParam String exchange,
        @RequestParam String symbol,
        @RequestParam String interval,
        @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
        @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' must be earlier than or equal to 'to'");
        }
        return candleQueryService.getCandles(exchange, symbol, interval, from, to);
    }
}
