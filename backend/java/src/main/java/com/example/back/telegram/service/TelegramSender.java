package com.example.back.telegram.service;

public interface TelegramSender {

    boolean sendText(String chatId, String text);
}
