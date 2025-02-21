package ru.x5.dnd.telegrambot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "bot")
public record BotProperties(
        String token,
        String username,
        String path,
        String chatId,
        Integer threadId,
        Set<String> masters
) {
}
