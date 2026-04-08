package com.example.back.telegram.bot;

import com.example.back.telegram.config.TelegramBotEnabledCondition;
import com.example.back.telegram.config.TelegramBotProperties;
import com.example.back.telegram.service.TelegramCommandResponse;
import com.example.back.telegram.service.TelegramCommandService;
import com.example.back.telegram.service.TelegramSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
@Conditional(TelegramBotEnabledCondition.class)
public class TradeLabTelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer, TelegramSender {

    private final TelegramBotProperties telegramBotProperties;
    private final TelegramCommandService telegramCommandService;
    private final ObjectMapper objectMapper;
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
        sendResponse(String.valueOf(message.getChatId()), response);
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        TelegramCommandResponse response = telegramCommandService.handleCallback(callbackQuery.getData());
        answerCallbackQuery(callbackQuery.getId());

        if (callbackQuery.getMessage() != null && response != null) {
            sendResponse(String.valueOf(callbackQuery.getMessage().getChatId()), response);
        }
    }

    private void sendResponse(String chatId, TelegramCommandResponse response) {
        if (response == null) {
            return;
        }

        if (response.photoResourcePath() != null && !response.photoResourcePath().isBlank()) {
            if (sendPhoto(chatId, response.text(), response.replyMarkup(), response.photoResourcePath())) {
                return;
            }
        }

        sendMessage(chatId, response.text(), response.replyMarkup());
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

    private boolean sendPhoto(String chatId, String caption, ReplyKeyboard replyMarkup, String photoResourcePath) {
        ClassPathResource photoResource = new ClassPathResource(photoResourcePath);
        if (!photoResource.exists()) {
            log.warn("Telegram welcome image {} was not found on classpath", photoResourcePath);
            return false;
        }

        try {
            byte[] photoBytes = photoResource.getInputStream().readAllBytes();
            ByteArrayResource telegramPhoto = new ByteArrayResource(photoBytes) {
                @Override
                public String getFilename() {
                    return photoResource.getFilename();
                }
            };

            MultiValueMap<String, Object> payload = new LinkedMultiValueMap<>();
            payload.add("chat_id", chatId);
            payload.add("caption", caption);
            payload.add("photo", telegramPhoto);

            if (replyMarkup != null) {
                payload.add("reply_markup", objectMapper.writeValueAsString(replyMarkup));
            }

            restClient.post()
                    .uri("/bot{token}/{method}", getBotToken(), SendPhoto.PATH)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(payload)
                    .retrieve()
                    .body(String.class);
            return true;
        } catch (IOException ex) {
            log.warn("Failed to read Telegram photo resource {}", photoResourcePath, ex);
            return false;
        } catch (RestClientException ex) {
            log.warn("Failed to send Telegram photo to chat {}", chatId, ex);
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
