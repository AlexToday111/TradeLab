package com.example.back.telegram.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram")
@Data
@NoArgsConstructor
public class TelegramBotProperties {
    private boolean enabled;
    private String botToken;
    private String botUserName;
    private String defaultChatId;

    public boolean hasToken() {
        return botToken != null && !botToken.isBlank();
    }

    public boolean hasDefaultChatId() {
        return defaultChatId != null && !defaultChatId.isBlank();
    }

    public boolean isBotStartupEnabled() {
        return enabled && hasToken();
    }

    public boolean isNotificationsEnabled() {
        return enabled && hasToken() && hasDefaultChatId();
    }
}
