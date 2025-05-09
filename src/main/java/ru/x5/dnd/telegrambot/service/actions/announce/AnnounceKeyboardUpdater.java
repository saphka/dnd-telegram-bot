package ru.x5.dnd.telegrambot.service.actions.announce;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.x5.dnd.telegrambot.config.CallbackConstants;
import ru.x5.dnd.telegrambot.model.GameStatus;
import ru.x5.dnd.telegrambot.service.TelegramService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class AnnounceKeyboardUpdater {

    private final MessageSource messageSource;
    private final TelegramService telegramService;

    public AnnounceKeyboardUpdater(MessageSource messageSource, TelegramService telegramService) {
        this.messageSource = messageSource;
        this.telegramService = telegramService;
    }

    public void updateGameMessageKeyboard(Long chatId, Integer messageId, GameStatus gameStatus, Integer currentPlayers, Integer maxPlayers) throws TelegramApiException {
        var keyboard = constructInlineKeyboardMarkup(gameStatus, currentPlayers, maxPlayers);

        var editKeyboard = new EditMessageReplyMarkup();
        editKeyboard.setChatId(chatId);
        editKeyboard.setMessageId(messageId);
        editKeyboard.setReplyMarkup(keyboard);
        telegramService.execute(editKeyboard);
    }

    private InlineKeyboardMarkup constructInlineKeyboardMarkup(GameStatus gameStatus, Integer currentPlayers, Integer maxPlayers) {
        if (GameStatus.CANCELLED.equals(gameStatus)) {
            return null;
        }
        var registerWithCharButton = new InlineKeyboardButton();
        registerWithCharButton.setCallbackData(CallbackConstants.CALLBACK_ANNOUNCE_REG_WITH_CHARACTER);
        registerWithCharButton.setText(messageSource.getMessage("announce.reg-char", new Object[]{currentPlayers, maxPlayers}, Locale.getDefault()));

        var registerWithPregenButton = new InlineKeyboardButton();
        registerWithPregenButton.setCallbackData(CallbackConstants.CALLBACK_ANNOUNCE_REG_WITH_PREGEN);
        registerWithPregenButton.setText(messageSource.getMessage("announce.reg-pregen", new Object[]{currentPlayers, maxPlayers}, Locale.getDefault()));

        var unRegisterButton = new InlineKeyboardButton();
        unRegisterButton.setCallbackData(CallbackConstants.CALLBACK_ANNOUNCE_REG_CANCEL);
        unRegisterButton.setText(messageSource.getMessage("announce.unreg", null, Locale.getDefault()));

        var gameCancelButton = new InlineKeyboardButton();
        gameCancelButton.setCallbackData(CallbackConstants.CALLBACK_ANNOUNCE_CANCEL);
        gameCancelButton.setText(messageSource.getMessage("announce.cancel", null, Locale.getDefault()));

        var keyboard = new InlineKeyboardMarkup();
        var lines = new ArrayList<List<InlineKeyboardButton>>();
        if (currentPlayers < maxPlayers) {
            lines.add(List.of(registerWithCharButton, registerWithPregenButton));
        }
        if (currentPlayers > 0) {
            lines.add(List.of(unRegisterButton));
        }
        lines.add(List.of(gameCancelButton));
        keyboard.setKeyboard(lines);
        return keyboard;
    }

}
