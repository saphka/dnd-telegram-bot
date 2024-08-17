package ru.x5.dnd.telegrambot.service.actions;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.x5.dnd.telegrambot.config.StateMachineEvents;
import ru.x5.dnd.telegrambot.config.StateMachineStates;
import ru.x5.dnd.telegrambot.config.TelegramMessageHeaders;
import ru.x5.dnd.telegrambot.service.TelegramService;

@Service
public class EchoAction implements Action<StateMachineStates, StateMachineEvents> {

    private final TelegramService telegramService;

    public EchoAction(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    @Override
    public void execute(StateContext<StateMachineStates, StateMachineEvents> context) {
        var update = (Update) context.getMessageHeader(TelegramMessageHeaders.UPDATE);
        var message = update.getMessage();
        var msg = new SendMessage();
        msg.setChatId(message.getChatId());
        msg.setText(message.getText());
        var removeKeyboard = new ReplyKeyboardRemove();
        removeKeyboard.setRemoveKeyboard(true);
        msg.setReplyMarkup(removeKeyboard);
        try {
            telegramService.execute(msg);
        } catch (TelegramApiException e) {
            throw new IllegalStateException(e);
        }
    }
}
