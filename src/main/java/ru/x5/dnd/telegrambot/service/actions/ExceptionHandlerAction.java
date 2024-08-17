package ru.x5.dnd.telegrambot.service.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.x5.dnd.telegrambot.config.StateMachineEvents;
import ru.x5.dnd.telegrambot.config.StateMachineStates;
import ru.x5.dnd.telegrambot.config.TelegramMessageHeaders;
import ru.x5.dnd.telegrambot.exception.BotLogicException;
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
        var e = context.getException();
        if (e != null) {
            log.error("Error occurred when processing update {}", context.getMessageHeader(TelegramMessageHeaders.UPDATE), e);

            var update = (Update) context.getMessageHeader(TelegramMessageHeaders.UPDATE);
            Message message;
            if (update.hasMessage()) {
                message = update.getMessage();
            } else if (update.hasCallbackQuery()) {
                message = (Message) update.getCallbackQuery().getMessage();
            } else {
                return;
            }
            var msg = new SendMessage();
            msg.setChatId(message.getChatId());
            msg.setMessageThreadId(message.getMessageThreadId());
            if (e instanceof BotLogicException) {
                msg.setText(e.getMessage());
            } else {
                msg.setText("Unknown error occurred. Try again later");
            }
            telegramService.executeAsync(msg);
        }
    }
}
