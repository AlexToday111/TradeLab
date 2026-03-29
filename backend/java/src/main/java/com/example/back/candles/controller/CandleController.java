package com.example.back.candles.controller;

import com.example.back.candles.dto.CandleResponse;
import com.example.back.candles.service.CandleQueryService;
import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/candles")
@Tag(
        name = "Candles",
        description = "Операции для получения исторических свечей по бирже, инструменту и интервалу"
)
public class CandleController {

    private final CandleQueryService candleQueryService;

    public CandleController(CandleQueryService candleQueryService) {
        this.candleQueryService = candleQueryService;
    }


    @Operation(
            summary = "Получить свечи",
            description = "Возвращает список свечей по указанным параметрам: биржа, символ, таймфрейм и временной диапазон"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Свечи успешно получены",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = CandleResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные параметры запроса"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    @GetMapping
    public List<CandleResponse> getCandles(
        @Parameter(
                description = "Название биржи",
                example = "binance",
                required = true
        )
        @RequestParam String exchange,

        @Parameter(
                description = "Торговый символ",
                example = "BTCUSDT",
                required = true
        )
        @RequestParam String symbol,

        @Parameter(
                description = "Интервал свечей",
                example = "1h",
                required = true
        )
        @RequestParam String interval,

        @Parameter(
                description = "Начало временного диапазона в ISO-8601",
                example = "2024-01-01T00:00:00Z",
                required = true
        )
        @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,

        @Parameter(
                description = "Конец временного диапазона в ISO-8601",
                example = "2024-01-31T23:59:59Z",
                required = true
        )
        @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        if (from.isAfter(to)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "'from' must be earlier than or equal to 'to'"
            );
        }
        return candleQueryService.getCandles(exchange, symbol, interval, from, to);
    }
}
