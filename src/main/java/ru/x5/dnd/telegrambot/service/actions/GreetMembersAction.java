package ru.x5.dnd.telegrambot.service.actions;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.x5.dnd.telegrambot.config.StateMachineEvents;
import ru.x5.dnd.telegrambot.config.StateMachineStates;
import ru.x5.dnd.telegrambot.config.TelegramMessageHeaders;
import ru.x5.dnd.telegrambot.service.GreetMemberService;

@Service
public class GreetMembersAction implements Action<StateMachineStates, StateMachineEvents> {

    private final GreetMemberService greetMemberService;

    public GreetMembersAction(GreetMemberService greetMemberService) {
        this.greetMemberService = greetMemberService;
    }

    @Override
    public void execute(StateContext<StateMachineStates, StateMachineEvents> context) {
        var update = (Update) context.getMessageHeader(TelegramMessageHeaders.UPDATE);
        var message = update.getMessage();
        greetMemberService.greet(message.getChatId(), message.getNewChatMembers());
    }


}
