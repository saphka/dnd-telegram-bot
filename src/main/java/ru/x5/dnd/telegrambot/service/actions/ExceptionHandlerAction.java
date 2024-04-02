package ru.x5.dnd.telegrambot.service.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.x5.dnd.telegrambot.config.StateMachineEvents;
import ru.x5.dnd.telegrambot.config.StateMachineStates;
import ru.x5.dnd.telegrambot.config.TelegramMessageHeaders;
import ru.x5.dnd.telegrambot.service.TelegramService;

@Service
public class ExceptionHandlerAction implements Action<StateMachineStates, StateMachineEvents> {

    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlerAction.class);

    private final TelegramService telegramService;

    public ExceptionHandlerAction(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    @Override
    public void execute(StateContext<StateMachineStates, StateMachineEvents> context) {
        Exception e = context.getException();
        if (e != null) {
            log.error("Error occurred when processing update {}", context.getMessageHeader(TelegramMessageHeaders.UPDATE), e);

            var message = (Message) context.getMessageHeader(TelegramMessageHeaders.MESSAGE);
            SendMessage msg = new SendMessage();
            msg.setChatId(message.getChatId());
            msg.setText("Unknown error occurred. Try again later");
            telegramService.executeAsync(msg);
        }
    }
}
