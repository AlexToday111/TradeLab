package com.example.back.telegram.service;

import com.example.back.runs.dto.RunResponse;
import com.example.back.telegram.config.TelegramBotProperties;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramNotificationService {

    private final TelegramBotProperties telegramBotProperties;
    private final TelegramSender telegramSender;
    private final TelegramMessageFormatter telegramMessageFormatter;

    public void sendRunStarted(RunResponse run) {
        sendNotification("run started", run, telegramMessageFormatter::formatRunStarted);
    }

    public void sendRunCompleted(RunResponse run) {
        sendNotification("run completed", run, telegramMessageFormatter::formatRunCompleted);
    }

    public void sendRunFailed(RunResponse run) {
        sendNotification("run failed", run, telegramMessageFormatter::formatRunFailed);
    }

    private void sendNotification(String notificationType, RunResponse run, Function<RunResponse, String> formatter) {
        if (!telegramBotProperties.isEnabled()) {
            return;
        }
        if (!telegramBotProperties.hasToken()) {
            log.debug("Telegram {} notification skipped: bot token is not configured", notificationType);
            return;
        }
        if (!telegramBotProperties.hasDefaultChatId()) {
            log.debug("Telegram {} notification skipped: default chat id is not configured", notificationType);
            return;
        }

        try {
            telegramSender.sendText(telegramBotProperties.getDefaultChatId(), formatter.apply(run));
        } catch (RuntimeException ex) {
            log.warn("Failed to send Telegram {} notification for run {}", notificationType, run.id(), ex);
        }
    }
}
