package com.example.back.telegram.bot;

import com.example.back.telegram.config.TelegramBotEnabledCondition;
import com.example.back.telegram.config.TelegramBotProperties;
import com.example.back.telegram.service.TelegramCommandResponse;
import com.example.back.telegram.service.TelegramCommandService;
import com.example.back.telegram.service.TelegramSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Slf4j
@Component
@RequiredArgsConstructor
@Conditional(TelegramBotEnabledCondition.class)
public class TradeLabTelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer, TelegramSender {

    private final TelegramBotProperties telegramBotProperties;
    private final TelegramCommandService telegramCommandService;
    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.telegram.org")
            .build();

    @Override
    public String getBotToken() {
        return telegramBotProperties.getBotToken();
    }

    @Override
    public LongPollingSingleThreadUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().getText() != null) {
                handleMessage(update.getMessage());
                return;
            }
            if (update.hasCallbackQuery()) {
                handleCallbackQuery(update.getCallbackQuery());
            }
        } catch (RuntimeException ex) {
            log.error("Failed to process Telegram update {}", update.getUpdateId(), ex);
        }
    }

    @Override
    public boolean sendText(String chatId, String text) {
        return sendMessage(chatId, text, null);
    }

    @AfterBotRegistration
    public void afterRegistration() {
        log.info("Telegram long polling bot started for username {}", telegramBotProperties.getBotUserName());
    }

    private void handleMessage(Message message) {
        TelegramCommandResponse response = telegramCommandService.handleMessage(message.getText());
        sendMessage(String.valueOf(message.getChatId()), response.text(), response.replyMarkup());
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        TelegramCommandResponse response = telegramCommandService.handleCallback(callbackQuery.getData());
        answerCallbackQuery(callbackQuery.getId());

        if (callbackQuery.getMessage() != null && response != null) {
            sendMessage(String.valueOf(callbackQuery.getMessage().getChatId()), response.text(), response.replyMarkup());
        }
    }

    private boolean sendMessage(String chatId, String text, ReplyKeyboard replyMarkup) {
        if (chatId == null || chatId.isBlank() || text == null || text.isBlank()) {
            return false;
        }

        SendMessage request = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(replyMarkup)
                .build();

        try {
            restClient.post()
                    .uri("/bot{token}/{method}", getBotToken(), SendMessage.PATH)
                    .body(request)
                    .retrieve()
                    .body(String.class);
            return true;
        } catch (RestClientException ex) {
            log.warn("Failed to send Telegram message to chat {}", chatId, ex);
            return false;
        }
    }

    private void answerCallbackQuery(String callbackQueryId) {
        try {
            restClient.post()
                    .uri("/bot{token}/{method}", getBotToken(), AnswerCallbackQuery.PATH)
                    .body(new AnswerCallbackQuery(callbackQueryId))
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException ex) {
            log.debug("Failed to answer Telegram callback query {}", callbackQueryId, ex);
        }
    }
}
