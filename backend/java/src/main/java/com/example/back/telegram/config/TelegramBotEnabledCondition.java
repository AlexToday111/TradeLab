package com.example.back.telegram.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class TelegramBotEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Boolean enabled = context.getEnvironment().getProperty("telegram.enabled", Boolean.class, false);
        String token = context.getEnvironment().getProperty("telegram.bot-token", "");
        return Boolean.TRUE.equals(enabled) && token != null && !token.isBlank();
    }
}
