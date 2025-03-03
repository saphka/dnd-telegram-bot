package ru.x5.dnd.telegrambot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "bot")
public record BotProperties(
        String token,
        String username,
        String path,
        String chatId, // 195533294
        Integer threadId,
        Set<String> masters // alexwardrune
) {
}
