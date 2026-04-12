package com.example.back.telegram.controller;

import com.example.back.telegram.config.TelegramBotProperties;
import com.example.back.telegram.dto.TelegramBotStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
@Tag(name = "Telegram", description = "Статус и доступность встроенного Telegram-бота")
public class TelegramBotController {

    private static final List<String> SUPPORTED_COMMANDS = List.of(
            "/runs",
            "/last",
            "/run <id>",
            "/help",
            "/settings"
    );

    private final TelegramBotProperties telegramBotProperties;

    @Operation(
            summary = "Получить статус Telegram-бота",
            description = "Возвращает статус конфигурации встроенного Telegram-бота компании"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус Telegram-бота получен")
    })
    @GetMapping("/status")
    public TelegramBotStatusResponse getStatus() {
        return new TelegramBotStatusResponse(
                "Trade360Lab Bot",
                normalizeUserName(telegramBotProperties.getBotUserName()),
                true,
                telegramBotProperties.isEnabled(),
                telegramBotProperties.hasToken(),
                telegramBotProperties.hasDefaultChatId(),
                telegramBotProperties.isBotStartupEnabled(),
                telegramBotProperties.isNotificationsEnabled(),
                SUPPORTED_COMMANDS
        );
    }

    private String normalizeUserName(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value.startsWith("@") ? value : "@" + value;
    }
}
