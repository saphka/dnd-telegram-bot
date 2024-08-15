package ru.x5.dnd.telegrambot.service;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.EntityType;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.x5.dnd.telegrambot.config.StateMachineEvents;
import ru.x5.dnd.telegrambot.config.TelegramMessageHeaders;

import java.util.Map;

@Service
public class TelegramUpdateProcessor {

    private final StateMachineService stateMachineService;

    public TelegramUpdateProcessor(StateMachineService stateMachineService) {
        this.stateMachineService = stateMachineService;
    }

    public void processUpdate(Update update) {
        if (!update.hasMessage()) {
            return;
        }
        var message = update.getMessage();
        var machineId = message.getChatId().toString();
        stateMachineService.sendToStateMachine(machineId, createMessage(message, update));
    }

    private org.springframework.messaging.Message<StateMachineEvents> createMessage(Message message, Update update) {
        var event = extractEvent(message);
        var headers = new MessageHeaders(Map.of(
                TelegramMessageHeaders.MESSAGE, message,
                TelegramMessageHeaders.UPDATE, update
        ));
        return new GenericMessage<>(event, headers);
    }

    private StateMachineEvents extractEvent(Message message) {
        if (!CollectionUtils.isEmpty(message.getNewChatMembers())) {
            return StateMachineEvents.NEW_MEMBERS;
        } else if (!CollectionUtils.isEmpty(message.getEntities())) {
            var command = message
                    .getEntities()
                    .stream()
                    .filter(e -> EntityType.BOTCOMMAND.equals(e.getType())).findFirst();
            if (command.isPresent()) {
                return EnumUtils.getEnum(StateMachineEvents.class, StateMachineEvents.COMMAND_PREFIX + command.get(), StateMachineEvents.UNKNOWN_COMMAND);
            }
        }
        return StateMachineEvents.TEXT_INPUT;
    }


}
