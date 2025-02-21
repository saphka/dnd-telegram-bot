package ru.x5.dnd.telegrambot.service.actions;

import org.springframework.context.MessageSource;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.x5.dnd.telegrambot.config.BotProperties;
import ru.x5.dnd.telegrambot.config.StateMachineEvents;
import ru.x5.dnd.telegrambot.config.StateMachineStates;
import ru.x5.dnd.telegrambot.config.TelegramMessageHeaders;
import ru.x5.dnd.telegrambot.exception.BotLogicException;
import ru.x5.dnd.telegrambot.model.Game;
import ru.x5.dnd.telegrambot.model.GameRegistration;
import ru.x5.dnd.telegrambot.service.GameService;
import ru.x5.dnd.telegrambot.service.TelegramService;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class StatsAction implements Action<StateMachineStates, StateMachineEvents> {

    private static final String ATTACH_FILE_EXT = ".csv";
    private static final String[] HEADERS = new String[]{
            "stats.header.msg",
            "stats.header.author",
            "stats.header.date",
            "stats.header.status",
            "stats.header.players"
    };
    private static final String SEPARATOR = ";";
    private static final String TG_URL_PREFIX = "https://t.me/c/";
    private static final int TG_CHAT_ID_STRIP = 4;
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault());

    private final GameService gameService;
    private final TelegramService telegramService;
    private final MessageSource messageSource;
    private final BotProperties botProperties;

    public StatsAction(GameService gameService, TelegramService telegramService, MessageSource messageSource, BotProperties botProperties) {
        this.gameService = gameService;
        this.telegramService = telegramService;
        this.messageSource = messageSource;
        this.botProperties = botProperties;
    }

    @Override
    @Transactional
    public void execute(StateContext<StateMachineStates, StateMachineEvents> context) {
        var update = (Update) context.getMessageHeader(TelegramMessageHeaders.UPDATE);
        var message = update.getMessage();

        if (!message.isUserMessage()) {
            throw new BotLogicException(messageSource.getMessage("error.stats.group", null, Locale.getDefault()));
        }
        if (!botProperties.masters().contains(message.getFrom().getUserName())) {
            throw new BotLogicException(messageSource.getMessage("error.stats.not-master", null, Locale.getDefault()));
        }

        var msg = new SendDocument();
        msg.setChatId(message.getChatId());
        msg.setMessageThreadId(message.getMessageThreadId());
        msg.setReplyToMessageId(message.getMessageId());

        final StringBuilder sb = new StringBuilder();
        addHeaders(sb);
        LocalDate dateAfter = LocalDate.now().minusYears(1);
        try (var games = gameService.findGamesAfter(botProperties.chatId(), dateAfter)) {
            games.forEach(game -> this.formatGameInfo(sb, game));
        }
        msg.setDocument(new InputFile(new ByteArrayInputStream(sb.toString().getBytes()),
                messageSource.getMessage("stats.file.name", new Object[]{formatter.format(dateAfter)}, Locale.getDefault()) + ATTACH_FILE_EXT));

        try {
            telegramService.execute(msg);
        } catch (TelegramApiException e) {
            throw new IllegalStateException(e);
        }
    }

    private void addHeaders(StringBuilder sb) {
        for (var header : HEADERS) {
            sb.append(messageSource.getMessage(header, null, Locale.getDefault()))
                    .append(SEPARATOR);
        }
    }

    private void formatGameInfo(StringBuilder sb, Game game) {
        sb.append("\n");
        sb.append(TG_URL_PREFIX)
                .append(game.getChatId().substring(TG_CHAT_ID_STRIP))
                .append("/")
                .append(game.getMessageId())
                .append(SEPARATOR);
        sb.append(game.getAuthor())
                .append(SEPARATOR);
        sb.append(formatter.format(game.getGameDate()))
                .append(SEPARATOR);
        sb.append(game.getStatus())
                .append(SEPARATOR);
        sb.append(game.getGameRegistrations().stream().map(GameRegistration::getGamerName).collect(Collectors.joining(" ")))
                .append(SEPARATOR);
    }
}
