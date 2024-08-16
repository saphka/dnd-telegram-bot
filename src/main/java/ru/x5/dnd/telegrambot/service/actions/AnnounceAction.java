package ru.x5.dnd.telegrambot.service.actions;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.MessageSource;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.x5.dnd.telegrambot.config.CallbackConstants;
import ru.x5.dnd.telegrambot.config.StateMachineEvents;
import ru.x5.dnd.telegrambot.config.StateMachineStates;
import ru.x5.dnd.telegrambot.config.TelegramMessageHeaders;
import ru.x5.dnd.telegrambot.exception.BotLogicException;
import ru.x5.dnd.telegrambot.service.GameService;
import ru.x5.dnd.telegrambot.service.TelegramService;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class AnnounceAction implements Action<StateMachineStates, StateMachineEvents> {

    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault());

    private final MessageSource messageSource;
    private final TelegramService telegramService;
    private final GameService gameService;

    public AnnounceAction(MessageSource messageSource, TelegramService telegramService, GameService gameService) {
        this.messageSource = messageSource;
        this.telegramService = telegramService;
        this.gameService = gameService;
    }

    @Override
    public void execute(StateContext<StateMachineStates, StateMachineEvents> context) {
        var update = (Update) context.getMessageHeader(TelegramMessageHeaders.UPDATE);
        var message = update.getMessage();
        var msg = constructMessage(message);
        var gameDate = extractGameDate(message);
        var author = extractAuthor(message);

        try {
            var announcement = telegramService.execute(msg);
            createGame(announcement, gameDate, author);
            telegramService.execute(new PinChatMessage(announcement.getChatId().toString(), announcement.getMessageId()));
            telegramService.execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
            telegramService.execute(new DeleteMessage(message.getChatId().toString(), message.getReplyToMessage().getMessageId()));

        } catch (TelegramApiException e) {
            throw new IllegalStateException(e);
        }
    }

    private void createGame(Message announcement, LocalDate gameDate, String author) {
        gameService.createGame(announcement.getChatId().toString(),
                announcement.getMessageId().toString(),
                announcement.getMessageThreadId().toString(),
                author,
                gameDate);
    }

    private String extractAuthor(Message message) {
        return message.getFrom().getUserName();
    }

    private LocalDate extractGameDate(Message message) {
        Integer botCommandOffset = message.getEntities().stream()
                .filter(e -> EntityType.BOTCOMMAND.equals(e.getType()))
                .findFirst()
                .map(com -> com.getOffset() + com.getLength())
                .orElse(0);
        String[] commandArgs = message.getText().substring(botCommandOffset + 1).split(" ");
        try {
            return LocalDate.parse(commandArgs[0], formatter);
        } catch (DateTimeParseException e) {
            throw new BotLogicException(messageSource.getMessage("error.announce.date-format", null, Locale.getDefault()), e);
        }
    }

    private SendPhoto constructMessage(Message message) {
        var msg = new SendPhoto();
        msg.setChatId(message.getChatId());
        msg.setMessageThreadId(message.getMessageThreadId());

        if (message.getReplyToMessage() == null) {
            throw new BotLogicException(messageSource.getMessage("error.announce.no-reply-to", null, Locale.getDefault()));
        }
        if (!message.isGroupMessage() && !message.isSuperGroupMessage()) {
            throw new BotLogicException(messageSource.getMessage("error.announce.not-group", null, Locale.getDefault()));
        }
        var replyToMessage = message.getReplyToMessage();
        if (CollectionUtils.isEmpty(replyToMessage.getPhoto())) {
            throw new BotLogicException(messageSource.getMessage("error.announce.no-photo", null, Locale.getDefault()));
        }
        msg.setCaption(replyToMessage.getCaption());
        var photoMaxRes = replyToMessage.getPhoto().stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .orElseThrow();
        try {
            var tgFile = telegramService.execute(new GetFile(photoMaxRes.getFileId()));
            File file = telegramService.downloadFile(tgFile);
            msg.setPhoto(new InputFile(file));
        } catch (TelegramApiException e) {
            throw new IllegalStateException(e);
        }
        var keyboard = constructInlineKeyboardMarkup();
        msg.setReplyMarkup(keyboard);

        return msg;
    }

    private InlineKeyboardMarkup constructInlineKeyboardMarkup() {
        var registerWithCharButton = new InlineKeyboardButton();
        registerWithCharButton.setCallbackData(CallbackConstants.CALLBACK_ANNOUNCE_REG_WITH_CHARACTER);
        registerWithCharButton.setText(messageSource.getMessage("announce.reg-char", null, Locale.getDefault()));

        var registerWithPregenButton = new InlineKeyboardButton();
        registerWithPregenButton.setCallbackData(CallbackConstants.CALLBACK_ANNOUNCE_REG_WITH_PREGEN);
        registerWithPregenButton.setText(messageSource.getMessage("announce.reg-pregen", null, Locale.getDefault()));

        var unRegisterButton = new InlineKeyboardButton();
        unRegisterButton.setCallbackData(CallbackConstants.CALLBACK_ANNOUNCE_REG_CANCEL);
        unRegisterButton.setText(messageSource.getMessage("announce.unreg", null, Locale.getDefault()));

        var keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(
                List.of(registerWithCharButton, registerWithPregenButton),
                List.of(unRegisterButton)));
        return keyboard;
    }
}
