package ru.x5.dnd.telegrambot.service.actions;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.x5.dnd.telegrambot.config.StateMachineEvents;
import ru.x5.dnd.telegrambot.config.StateMachineStates;
import ru.x5.dnd.telegrambot.config.TelegramMessageHeaders;
import ru.x5.dnd.telegrambot.service.TelegramService;

import java.text.MessageFormat;
import java.util.stream.Collectors;

@Service
public class GreetMembersAction implements Action<StateMachineStates, StateMachineEvents> {

    private final TelegramService telegramService;

    public GreetMembersAction(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    @Override
    public void execute(StateContext<StateMachineStates, StateMachineEvents> context) {
        var message = (Message) context.getMessageHeader(TelegramMessageHeaders.MESSAGE);
        var msg = new SendMessage();
        msg.setChatId(message.getChatId());
        msg.setText(MessageFormat.format("Добро пожаловать, {0}!", formatMembers(message)));
        try {
            telegramService.execute(msg);
        } catch (TelegramApiException e) {
            throw new IllegalStateException(e);
        }
    }

    private String formatMembers(Message message) {
        return message
                .getNewChatMembers()
                .stream()
                .map(mem -> "@" + mem.getUserName())
                .collect(Collectors.joining(", "));
    }
}
