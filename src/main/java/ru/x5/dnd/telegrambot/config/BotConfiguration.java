package ru.x5.dnd.telegrambot.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.x5.dnd.telegrambot.adapter.TelegramLongPollingAdapter;

@Configuration
@EnableConfigurationProperties(BotProperties.class)
public class BotConfiguration {
    @Bean
    @Profile("prod")
    public TelegramBotsApi telegramBotsApi(TelegramLongPollingAdapter longPollingAdapter) throws TelegramApiException {
        var botApi = new TelegramBotsApi(DefaultBotSession.class);
        botApi.registerBot(longPollingAdapter);
        return botApi;
    }
}
