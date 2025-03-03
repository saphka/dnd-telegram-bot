package ru.x5.dnd.telegrambot.service.actions.announce;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.MessageSource;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.x5.dnd.telegrambot.config.BotProperties;
import ru.x5.dnd.telegrambot.config.StateMachineEvents;
import ru.x5.dnd.telegrambot.config.StateMachineStates;
import ru.x5.dnd.telegrambot.config.TelegramMessageHeaders;
import ru.x5.dnd.telegrambot.exception.BotLogicException;
import ru.x5.dnd.telegrambot.model.Game;
import ru.x5.dnd.telegrambot.service.GameService;
import ru.x5.dnd.telegrambot.service.TelegramService;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.Locale;

@Service
public class AnnounceAction implements Action<StateMachineStates, StateMachineEvents> {

    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault());
    private static final int ANNOUNCE_COMMAND_ARGS_COUNT = 2;
    public static final int MAX_PHOTO_CAPTION_LENGTH = 300;

    private final MessageSource messageSource;
    private final TelegramService telegramService;
    private final GameService gameService;
    private final BotProperties botProperties;
    private final AnnounceKeyboardUpdater keyboardUpdater;

    public AnnounceAction(MessageSource messageSource, TelegramService telegramService, GameService gameService, BotProperties botProperties, AnnounceKeyboardUpdater keyboardUpdater) {
        this.messageSource = messageSource;
        this.telegramService = telegramService;
        this.gameService = gameService;
        this.botProperties = botProperties;
        this.keyboardUpdater = keyboardUpdater;
    }

    @Override
    public void execute(StateContext<StateMachineStates, StateMachineEvents> context) {
        var update = (Update) context.getMessageHeader(TelegramMessageHeaders.UPDATE);
        var message = update.getMessage();
        var author = extractAuthor(message);
        var photo = constructMessage(message, author);
        var gameInfo = extractGameInfo(message);

        SendMessage msg = null;
        if (photo.getCaption().length() > MAX_PHOTO_CAPTION_LENGTH) {
            msg = copyCaptionToSeparateMessage(photo);
        }

        try {
            var announcement = telegramService.execute(photo);
            if (msg != null) {
                msg.setReplyToMessageId(announcement.getMessageId());
                announcement = telegramService.execute(msg);
            }
            var game = createGame(announcement, gameInfo.gameDate(), author, gameInfo.maxPlayers());
            keyboardUpdater.updateGameMessageKeyboard(announcement.getChatId(), announcement.getMessageId(), game.getStatus(), 0, gameInfo.maxPlayers);
            telegramService.execute(new PinChatMessage(announcement.getChatId().toString(), announcement.getMessageId()));
            telegramService.execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
            telegramService.execute(new DeleteMessage(message.getChatId().toString(), message.getReplyToMessage().getMessageId()));
        } catch (TelegramApiException e) {
            throw new IllegalStateException(e);
        }
    }

    private static SendMessage copyCaptionToSeparateMessage(SendPhoto photo) {
        SendMessage msg;
        msg = new SendMessage();
        msg.setChatId(photo.getChatId());
        msg.setMessageThreadId(photo.getMessageThreadId());
        msg.setText(photo.getCaption());
        msg.setParseMode(photo.getParseMode());
        photo.setCaption("");
        return msg;
    }

    private Game createGame(Message announcement, LocalDate gameDate, String author, Integer maxPlayers) {
        return gameService.createGame(announcement.getChatId().toString(),
                announcement.getMessageId().toString(),
                announcement.getMessageThreadId().toString(),
                author,
                gameDate, maxPlayers);
    }

    private String extractAuthor(Message message) {
        return message.getFrom().getUserName();
    }

    private GameInfo extractGameInfo(Message message) {
        Integer botCommandOffset = message.getEntities().stream()
                .filter(e -> EntityType.BOTCOMMAND.equals(e.getType()))
                .findFirst()
                .map(com -> com.getOffset() + com.getLength())
                .orElse(0);
        String[] commandArgs = message.getText().substring(botCommandOffset).trim().split(" ");
        if (commandArgs.length < ANNOUNCE_COMMAND_ARGS_COUNT) {
            throw new BotLogicException(messageSource.getMessage("error.announce.args-count", null, Locale.getDefault()));
        }
        LocalDate gameDate;
        try {
            gameDate = LocalDate.parse(commandArgs[0], formatter);
        } catch (DateTimeParseException e) {
            throw new BotLogicException(messageSource.getMessage("error.announce.date-format", null, Locale.getDefault()), e);
        }
        int maxPlayers;
        try {
            maxPlayers = Integer.parseInt(commandArgs[1]);
        } catch (NumberFormatException e) {
            throw new BotLogicException(messageSource.getMessage("error.announce.max-players", null, Locale.getDefault()), e);
        }
        return new GameInfo(gameDate, maxPlayers);
    }

    private SendPhoto constructMessage(Message message, String author) {
        var msg = new SendPhoto();
        msg.setChatId(botProperties.chatId());
        msg.setMessageThreadId(botProperties.threadId());

        if (message.getReplyToMessage() == null) {
            throw new BotLogicException(messageSource.getMessage("error.announce.no-reply-to", null, Locale.getDefault()));
        }
        if (!message.isUserMessage()) {
            throw new BotLogicException(messageSource.getMessage("error.announce.group", null, Locale.getDefault()));
        }
        if (!botProperties.masters().contains(author)) {
            throw new BotLogicException(messageSource.getMessage("error.announce.not-master", null, Locale.getDefault()));
        }
        var replyToMessage = message.getReplyToMessage();
        if (CollectionUtils.isEmpty(replyToMessage.getPhoto())) {
            throw new BotLogicException(messageSource.getMessage("error.announce.no-photo", null, Locale.getDefault()));
        }
        copyCaption(msg, replyToMessage);
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

        return msg;
    }

    private static void copyCaption(SendPhoto msg, Message replyToMessage) {
        if (CollectionUtils.isNotEmpty(replyToMessage.getCaptionEntities())) {
            msg.setParseMode(ParseMode.HTML);
            String text = replyToMessage.getCaption();
            StringBuilder sb = new StringBuilder();
            int offset = 0;
            for (var entity : replyToMessage.getCaptionEntities()) {
                sb.append(text, offset, entity.getOffset());
                offset = entity.getOffset();
                switch (entity.getType()) {
                    case EntityType.BOLD:
                        sb.append("<b>").append(entity.getText()).append("</b>");
                        offset += entity.getLength();
                        break;
                    case EntityType.ITALIC:
                        sb.append("<i>").append(entity.getText()).append("</i>");
                        offset += entity.getLength();
                        break;
                }
            }
            sb.append(text, offset, text.length());
            msg.setCaption(sb.toString());
        } else {
            msg.setCaption(replyToMessage.getCaption());
        }
    }

    private record GameInfo(LocalDate gameDate, Integer maxPlayers) {
    }
}
