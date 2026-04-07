package com.example.back.backtest.controller;

import com.example.back.backtest.dto.BacktestCreatedResponse;
import com.example.back.backtest.dto.BacktestRunResponse;
import com.example.back.backtest.dto.BacktestTrade;
import com.example.back.backtest.dto.CreateBacktestRunRequest;
import com.example.back.backtest.dto.EquityPoint;
import com.example.back.backtest.service.BacktestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/backtests")
@RequiredArgsConstructor
@Tag(name = "Backtests", description = "Запуск и просмотр результатов бэктестов")
public class BacktestController {

    private final BacktestService backtestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Создать и выполнить бэктест",
            description = "Создаёт запись в runs, синхронно запускает Python backtesting engine и сохраняет результат"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Бэктест успешно выполнен",
                    content = @Content(schema = @Schema(implementation = BacktestCreatedResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "404", description = "Стратегия или запуск не найдены"),
            @ApiResponse(responseCode = "500", description = "Ошибка выполнения Python")
    })
    public BacktestCreatedResponse createBacktest(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Параметры запуска бэктеста",
                    content = @Content(
                            schema = @Schema(implementation = CreateBacktestRunRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "strategyId": 42,
                                      "exchange": "binance",
                                      "symbol": "BTCUSDT",
                                      "interval": "1h",
                                      "from": "2024-01-01T00:00:00Z",
                                      "to": "2024-01-03T00:00:00Z",
                                      "params": {
                                        "fastPeriod": 10,
                                        "slowPeriod": 21
                                      },
                                      "initialCash": 10000.0,
                                      "feeRate": 0.001,
                                      "slippageBps": 5.0,
                                      "strictData": true
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody CreateBacktestRunRequest request
    ) {
        Long runId = backtestService.createRun(request);
        backtestService.executeRun(runId);
        return new BacktestCreatedResponse(runId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить запуск", description = "Возвращает статус, summary и сообщение об ошибке")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Запуск найден",
                    content = @Content(schema = @Schema(implementation = BacktestRunResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Запуск не найден")
    })
    public BacktestRunResponse getRun(
            @Parameter(description = "ID запуска", example = "101")
            @PathVariable Long id
    ) {
        return backtestService.getRun(id);
    }

    @GetMapping("/{id}/trades")
    @Operation(summary = "Получить сделки", description = "Возвращает сделки конкретного запуска")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Сделки получены",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BacktestTrade.class)))
            ),
            @ApiResponse(responseCode = "404", description = "Запуск не найден")
    })
    public List<BacktestTrade> getTrades(
            @Parameter(description = "ID запуска", example = "101")
            @PathVariable Long id
    ) {
        return backtestService.getTrades(id);
    }

    @GetMapping("/{id}/equity")
    @Operation(summary = "Получить equity curve", description = "Возвращает кривую капитала для запуска")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Кривая капитала получена",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = EquityPoint.class)))
            ),
            @ApiResponse(responseCode = "404", description = "Запуск не найден")
    })
    public List<EquityPoint> getEquity(
            @Parameter(description = "ID запуска", example = "101")
            @PathVariable Long id
    ) {
        return backtestService.getEquity(id);
    }
}
