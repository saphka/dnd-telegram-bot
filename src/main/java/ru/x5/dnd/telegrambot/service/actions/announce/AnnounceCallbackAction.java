package ru.x5.dnd.telegrambot.service.actions.announce;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.MessageSource;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.x5.dnd.telegrambot.config.CallbackConstants;
import ru.x5.dnd.telegrambot.config.StateMachineEvents;
import ru.x5.dnd.telegrambot.config.StateMachineStates;
import ru.x5.dnd.telegrambot.config.TelegramMessageHeaders;
import ru.x5.dnd.telegrambot.model.Game;
import ru.x5.dnd.telegrambot.model.GameRegistration;
import ru.x5.dnd.telegrambot.model.RegistrationType;
import ru.x5.dnd.telegrambot.service.GameService;
import ru.x5.dnd.telegrambot.service.TelegramService;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class AnnounceCallbackAction implements Action<StateMachineStates, StateMachineEvents> {

    private final TelegramService telegramService;
    private final GameService gameService;
    private final MessageSource messageSource;
    private final AnnounceKeyboardUpdater keyboardUpdater;

    public AnnounceCallbackAction(TelegramService telegramService, GameService gameService, MessageSource messageSource, AnnounceKeyboardUpdater keyboardUpdater) {
        this.telegramService = telegramService;
        this.gameService = gameService;
        this.messageSource = messageSource;
        this.keyboardUpdater = keyboardUpdater;
    }

    @Override
    public void execute(StateContext<StateMachineStates, StateMachineEvents> context) {
        var update = (Update) context.getMessageHeader(TelegramMessageHeaders.UPDATE);
        var message = (Message) update.getCallbackQuery().getMessage();

        var game = processUserRegistration(update, message);
        var msg = constructMessage(update, message, game);

        try {
            keyboardUpdater.updateGameMessageKeyboard(Long.parseLong(game.getChatId()), Integer.parseInt(game.getMessageId()), game.getStatus(), game.getGameRegistrations().size(), game.getMaxPlayers());
            telegramService.execute(msg);
        } catch (TelegramApiException e) {
            throw new IllegalStateException(e);
        }
    }

    private Game processUserRegistration(Update update, Message message) {
        if (CallbackConstants.CALLBACK_ANNOUNCE_REG_WITH_CHARACTER.equals(update.getCallbackQuery().getData())) {
            return gameService.addUserToGame(message.getChatId().toString(),
                    message.getMessageId().toString(),
                    message.getMessageThreadId().toString(),
                    update.getCallbackQuery().getFrom().getUserName(),
                    RegistrationType.CHARACTER);
        } else if (CallbackConstants.CALLBACK_ANNOUNCE_REG_WITH_PREGEN.equals(update.getCallbackQuery().getData())) {
            return gameService.addUserToGame(message.getChatId().toString(),
                    message.getMessageId().toString(),
                    message.getMessageThreadId().toString(),
                    update.getCallbackQuery().getFrom().getUserName(),
                    RegistrationType.PREGEN);
        } else if (CallbackConstants.CALLBACK_ANNOUNCE_REG_CANCEL.equals(update.getCallbackQuery().getData())) {
            return gameService.removeUserFromGame(message.getChatId().toString(),
                    message.getMessageId().toString(),
                    message.getMessageThreadId().toString(),
                    update.getCallbackQuery().getFrom().getUserName());
        } else if (CallbackConstants.CALLBACK_ANNOUNCE_CANCEL.equals(update.getCallbackQuery().getData())) {
            return gameService.cancelGame(message.getChatId().toString(),
                    message.getMessageId().toString(),
                    message.getMessageThreadId().toString(),
                    update.getCallbackQuery().getFrom().getUserName());
        }
        throw new IllegalArgumentException("Unknown callback query bot command:" + update.getCallbackQuery().getData());
    }

    private SendMessage constructMessage(Update update, Message message, Game game) {
        var msg = new SendMessage();
        msg.setChatId(message.getChatId());
        msg.setMessageThreadId(message.getMessageThreadId());
        msg.setReplyToMessageId(message.getMessageId());

        if (CallbackConstants.CALLBACK_ANNOUNCE_REG_WITH_CHARACTER.equals(update.getCallbackQuery().getData())) {
            msg.setText(messageSource.getMessage("announce.reg-char.confirm", new Object[]{update.getCallbackQuery().getFrom().getUserName(), game.getAuthor()}, Locale.getDefault()));
        } else if (CallbackConstants.CALLBACK_ANNOUNCE_REG_WITH_PREGEN.equals(update.getCallbackQuery().getData())) {
            msg.setText(messageSource.getMessage("announce.reg-pregen.confirm", new Object[]{update.getCallbackQuery().getFrom().getUserName(), game.getAuthor()}, Locale.getDefault()));
        } else if (CallbackConstants.CALLBACK_ANNOUNCE_REG_CANCEL.equals(update.getCallbackQuery().getData())) {
            msg.setText(messageSource.getMessage("announce.unreg.confirm", new Object[]{update.getCallbackQuery().getFrom().getUserName()}, Locale.getDefault()));
        } else if (CallbackConstants.CALLBACK_ANNOUNCE_CANCEL.equals(update.getCallbackQuery().getData())) {
            var msgText = messageSource.getMessage("announce.game-cancelled", null, Locale.getDefault());
            if (CollectionUtils.isNotEmpty(game.getGameRegistrations())) {
                msgText += " " + messageSource.getMessage("announce.game-cancelled-sorry", new Object[]{formatMembers(game.getGameRegistrations())}, Locale.getDefault());
            }
            msg.setText(msgText);
        }

        return msg;
    }

    private String formatMembers(List<GameRegistration> users) {
        return users
                .stream()
                .map(GameRegistration::getGamerName)
                .map(mem -> "@" + mem)
                .collect(Collectors.joining(", "));
    }
}
