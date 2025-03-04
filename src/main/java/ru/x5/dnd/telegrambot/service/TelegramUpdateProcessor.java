package ru.x5.dnd.telegrambot.service;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.EntityType;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.x5.dnd.telegrambot.config.CallbackConstants;
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
        String machineId = getMachineId(update);
        if (machineId == null) {
            return;
        }
        stateMachineService.sendToStateMachine(machineId, createMessage(update));
    }

    private String getMachineId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId().toString();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId().toString();
        }
        return null;
    }

    private Message<StateMachineEvents> createMessage(Update update) {
        var event = extractEvent(update);
        var headers = new MessageHeaders(Map.of(
                TelegramMessageHeaders.UPDATE, update
        ));
        return new GenericMessage<>(event, headers);
    }

    private StateMachineEvents extractEvent(Update update) {
        var message = update.getMessage();
        if (message != null) {
            if (CollectionUtils.isNotEmpty(message.getNewChatMembers())) {
                return StateMachineEvents.NEW_MEMBERS;
            } else if (CollectionUtils.isNotEmpty(message.getEntities())) {
                var command = message
                        .getEntities()
                        .stream()
                        .filter(e -> EntityType.BOTCOMMAND.equals(e.getType())).findFirst();
                if (command.isPresent()) {
                    return getStateMachineEvent(StateMachineEvents.COMMAND_PREFIX + extractCommand(command.get()));
                }
            } else if (StringUtils.isNotEmpty(message.getText())) {
                return StateMachineEvents.TEXT_INPUT;
            }
        } else if (update.hasCallbackQuery()) {
            var query = update.getCallbackQuery();
            String[] split = query.getData().split("\\" + CallbackConstants.CALLBACK_SEPARATOR, 2);
            return getStateMachineEvent(StateMachineEvents.CALLBACK_PREFIX + split[0]);
        }
        return StateMachineEvents.UNKNOWN;
    }

    private static String extractCommand(MessageEntity command) {
        var commandTest = command.getText().substring(1);
        var mentionIndex = commandTest.indexOf('@');
        if (mentionIndex != -1) {
            return commandTest.substring(0, mentionIndex);
        }
        return commandTest;
    }

    private StateMachineEvents getStateMachineEvent(final String stringCommand) {
        return EnumUtils.getEnum(
                StateMachineEvents.class,
                StateMachineEvents.getStateEventByCommand(stringCommand).toString(),
                StateMachineEvents.UNKNOWN
        );
    }

}
