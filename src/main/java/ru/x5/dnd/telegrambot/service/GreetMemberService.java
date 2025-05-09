package ru.x5.dnd.telegrambot.service;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.x5.dnd.telegrambot.config.BotProperties;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class GreetMemberService {

    private final static Logger log = LoggerFactory.getLogger(GreetMemberService.class);

    public static final int GREETING_DELAY = 30000;
    private final TelegramService telegramService;
    private final MessageSource messageSource;
    private final BotProperties botProperties;
    private final Map<Long, Set<String>> greetings = new ConcurrentHashMap<>();

    public GreetMemberService(TelegramService telegramService,
                              MessageSource messageSource,
                              BotProperties botProperties) {
        this.telegramService = telegramService;
        this.messageSource = messageSource;
        this.botProperties = botProperties;
    }

    public void greet(Long chatId, List<User> users) {
        var newUserNames = users.stream().map(User::getUserName).toList();
        addUsersToGreetings(chatId, newUserNames);
    }

    private void addUsersToGreetings(Long chatId, Collection<String> newUserNames) {
        greetings.compute(chatId, (chatIdKey, usersValue) -> {
            if (usersValue == null) {
                usersValue = new HashSet<>();
            }
            usersValue.addAll(newUserNames);
            return usersValue;
        });
    }

    @Scheduled(fixedDelay = GREETING_DELAY)
    public void greetDelayed() {
        for (Long chatId : greetings.keySet()) {
            var users = greetings.remove(chatId);
            if (CollectionUtils.isNotEmpty(users)) {
                try {
                    greetByChatId(chatId, users);
                } catch (TelegramApiException e) {
                    log.error("Cannot greet chat id {}, new members {}", chatId, users, e);
                    addUsersToGreetings(chatId, users);
                }
            }
        }
    }

    private void greetByChatId(Long chatId, Set<String> users) throws TelegramApiException {
        var msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(messageSource.getMessage(
                "greeting.new-member",
                new Object[]{ formatMembers(users), botProperties.username() },
                Locale.getDefault())
        );
        msg.setParseMode(ParseMode.HTML);
        telegramService.execute(msg);
    }

    private String formatMembers(Set<String> users) {
        return users
                .stream()
                .map(mem -> "@" + mem)
                .collect(Collectors.joining(", "));
    }

}
