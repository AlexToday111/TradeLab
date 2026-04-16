package com.example.back.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Стандартный JSON-ответ с ошибкой")
public record ApiErrorResponse(
        @Schema(description = "Время ошибки", example = "2024-01-01T00:00:01Z")
        Instant timestamp,
        @Schema(description = "HTTP статус", example = "400")
        int status,
        @Schema(description = "Краткое описание", example = "Bad Request")
        String error,
        @Schema(description = "Сообщение", example = "Field 'from' must be before 'to'")
        String message,
        @Schema(description = "Correlation identifier for tracing the request", example = "req-123")
        String correlationId,
        @Schema(description = "Путь запроса", example = "/backtests")
        String path
) {
}
