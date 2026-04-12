package com.example.back.telegram.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Статус встроенного Telegram-бота компании")
public record TelegramBotStatusResponse(
        @Schema(description = "Отображаемое имя бота", example = "Trade360Lab Bot")
        String botName,
        @Schema(description = "Username бота в Telegram", example = "@trade360lab_bot")
        String botUserName,
        @Schema(description = "Бот доступен в backend-коде")
        boolean codeAvailable,
        @Schema(description = "Telegram-интеграция включена через конфигурацию")
        boolean enabled,
        @Schema(description = "Для бота задан bot token")
        boolean tokenConfigured,
        @Schema(description = "Для уведомлений задан chat id")
        boolean defaultChatConfigured,
        @Schema(description = "Достаточно настроек для запуска long polling бота")
        boolean botStartupEnabled,
        @Schema(description = "Достаточно настроек для отправки уведомлений")
        boolean notificationsEnabled,
        @Schema(description = "Поддерживаемые команды")
        List<String> commands
) {
}
