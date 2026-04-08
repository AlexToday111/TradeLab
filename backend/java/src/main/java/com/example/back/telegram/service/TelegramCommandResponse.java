package com.example.back.telegram.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

public record TelegramCommandResponse(
        String text,
        ReplyKeyboard replyMarkup
) {
}
