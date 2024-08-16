package ru.x5.dnd.telegrambot.service;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class GreetMemberService {

    public static final int GREETING_DELAY = 30000;
    private final TelegramService telegramService;
    private final MessageSource messageSource;
    private final Map<Long, Set<String>> greetings = new ConcurrentHashMap<>();

    public GreetMemberService(TelegramService telegramService, MessageSource messageSource) {
        this.telegramService = telegramService;
        this.messageSource = messageSource;
    }

    public void greet(Long chatId, List<User> users) {
        greetings.compute(chatId, (chatIdKey, usersValue) -> {
            if (usersValue == null) {
                usersValue = new HashSet<>();
            }
            usersValue.addAll(users.stream().map(User::getUserName).toList());
            return usersValue;
        });
    }

    @Scheduled(fixedDelay = GREETING_DELAY)
    public void greetDelayed() {
        for (Long chatId : greetings.keySet()) {
            Set<String> users = greetings.remove(chatId);
            if (CollectionUtils.isNotEmpty(users)) {
                greetByChatId(chatId, users);
            }
        }
    }

    private void greetByChatId(Long chatId, Set<String> users) {
        var msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(messageSource.getMessage("greeting.new-member", new Object[]{formatMembers(users)}, Locale.getDefault()));
        try {
            telegramService.execute(msg);
        } catch (TelegramApiException e) {
            throw new IllegalStateException(e);
        }
    }

    private String formatMembers(Set<String> users) {
        return users
                .stream()
                .map(mem -> "@" + mem)
                .collect(Collectors.joining(", "));
    }

}
