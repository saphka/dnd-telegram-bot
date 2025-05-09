package ru.x5.dnd.telegrambot.service.actions.search;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.x5.dnd.telegrambot.config.StateMachineEvents;
import ru.x5.dnd.telegrambot.config.StateMachineStates;
import ru.x5.dnd.telegrambot.config.TelegramMessageHeaders;
import ru.x5.dnd.telegrambot.service.SearchInfoService;

@Service
public class SearchInfoAction implements Action<StateMachineStates, StateMachineEvents> {
    private final SearchInfoService searchInfoService;

    public SearchInfoAction(SearchInfoService searchInfoService) {
        this.searchInfoService = searchInfoService;
    }

    @Override
    public void execute(StateContext<StateMachineStates, StateMachineEvents> context) {
        var update = (Update) context.getMessageHeader(TelegramMessageHeaders.UPDATE);
        var message = update.getMessage();

        if (message == null) {
            var callBackMessage = (Message) update.getCallbackQuery().getMessage();
            var callBackCommand = update.getCallbackQuery().getData();
            searchInfoService.callBackAnswer(callBackMessage, callBackCommand);
        } else {
            searchInfoService.answer(message);
        }
    }
}
