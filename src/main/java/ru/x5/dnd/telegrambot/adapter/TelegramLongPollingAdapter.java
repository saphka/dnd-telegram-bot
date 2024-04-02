package ru.x5.dnd.telegrambot.adapter;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.x5.dnd.telegrambot.config.BotProperties;
import ru.x5.dnd.telegrambot.service.TelegramUpdateProcessor;

@Service
public class TelegramLongPollingAdapter extends TelegramLongPollingBot {

    private final BotProperties properties;
    private final TelegramUpdateProcessor telegramUpdateProcessor;

    public TelegramLongPollingAdapter(BotProperties properties, TelegramUpdateProcessor telegramUpdateProcessor) {
        super(properties.token());
        this.properties = properties;
        this.telegramUpdateProcessor = telegramUpdateProcessor;
    }

    @Override
    public void onUpdateReceived(Update update) {
        telegramUpdateProcessor.processUpdate(update);
    }

    @Override
    public String getBotUsername() {
        return properties.username();
    }
}
