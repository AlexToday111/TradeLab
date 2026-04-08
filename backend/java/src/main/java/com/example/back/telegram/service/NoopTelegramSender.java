package com.example.back.telegram.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnMissingBean(TelegramSender.class)
public class NoopTelegramSender implements TelegramSender {

    @Override
    public boolean sendText(String chatId, String text) {
        log.debug("Telegram sender is not available, skipping message to chat {}", chatId);
        return false;
    }
}
