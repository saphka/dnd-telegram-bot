package ru.x5.dnd.telegrambot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.x5.dnd.telegrambot.config.StateMachineEvents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.x5.dnd.telegrambot.config.StateMachineEvents.COMMAND_SEARCH_DND_5E_RULE;
import static ru.x5.dnd.telegrambot.config.StateMachineEvents.COMMAND_SEARCH_GAME_RULE;
import static ru.x5.dnd.telegrambot.config.StateMachineEvents.COMMAND_SEARCH_INFO;
import static ru.x5.dnd.telegrambot.utils.ButtonUtils.createTextButton;
import static ru.x5.dnd.telegrambot.utils.ButtonUtils.createUrlButton;

@Service
public class SearchInfoService {
    private final static Logger LOG = LoggerFactory.getLogger(SearchInfoService.class);

    private final TelegramService telegramService;
    private final PropertyMessageService propertyMessageService;

    private static final Map<StateMachineEvents, String> MESSAGE_MAP = new HashMap<>();

    public SearchInfoService(TelegramService telegramService,
                             PropertyMessageService propertyMessageService) {
        this.telegramService = telegramService;
        this.propertyMessageService = propertyMessageService;

        fillMessageMap();
    }

    private void fillMessageMap() {
        LOG.info("\nPrepare message map by commands for 'SearchInfoService'");

        MESSAGE_MAP.put(COMMAND_SEARCH_INFO, propertyMessageService.getMessageByName("search.info.help"));
        MESSAGE_MAP.put(COMMAND_SEARCH_DND_5E_RULE, propertyMessageService.getMessageByName("search.dnd.5e.rule"));
        MESSAGE_MAP.put(COMMAND_SEARCH_GAME_RULE, propertyMessageService.getMessageByName("search.game.info"));

        LOG.info("Prepare message map by commands - done\n");
    }

    /**
     * Отображение всех доступных команд поиска
     *
     * @param chatId id чата
     */
    public void answer(final Long chatId, final StateMachineEvents command) {
        var msg = prepareSendMessage(chatId, command);
        addButtons(command, msg);

        try {
            telegramService.execute(msg);
        } catch (TelegramApiException e) {
            LOG.error("Cannot send message for chat id: {}", chatId, e);
        }
    }

    /**
     * Отправка сообщения пользователю в чат при событии: "нажата кнопка"
     *
     * @param chatId id чата, в который возвращается ответ
     * @param command событие машины состояний
     */
    public void callBackAnswer(final Long chatId, final StateMachineEvents command) {
        var msg = prepareSendMessage(chatId, command);
        addButtons(command, msg);

        try {
            telegramService.execute(msg);
        } catch (TelegramApiException e) {
            LOG.error("Cannot send message for chat id: {}", chatId, e);
        }
    }

    private SendMessage prepareSendMessage(final Long chatId, final StateMachineEvents command) {
        var msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(getMessageByCommand(command));
        msg.setParseMode(ParseMode.HTML);
        return msg;
    }

    private String getMessageByCommand(final StateMachineEvents command) {
        return MESSAGE_MAP.containsKey(command) ? MESSAGE_MAP.get(command) : MESSAGE_MAP.get(COMMAND_SEARCH_INFO);
    }

    /**
     * Создание кнопок
     *
     * @param command событие машины состояний
     * @param msg отправлемое сообщение в чат
     */
    private void addButtons(final StateMachineEvents command, final SendMessage msg) {
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        if (command == COMMAND_SEARCH_GAME_RULE) {
            createSearchGameRuleButtons(rowList);
        } else if (command == COMMAND_SEARCH_DND_5E_RULE) {
            createSearchDNDRuleButtons(rowList);
        } else {
            rowList.add(
                    List.of(
                            createTextButton("Правила D&D 5E", "sdnd5erule"),
                            createTextButton("Информация по играм", "sgrule")
                    )
            );

            rowList.add(List.of(createTextButton("Все команды", "help")));
        }

        msg.setReplyMarkup(InlineKeyboardMarkup.builder()
                .keyboard(rowList)
                .build()
        );
    }

    private void createSearchDNDRuleButtons(final List<List<InlineKeyboardButton>> rowList) {
        rowList.add(List.of(createUrlButton("Как играть?", "https://t.me/c/1856208477/4859/6206")));
        rowList.add(List.of(
                createUrlButton("Создание персонажа", "https://t.me/c/1856208477/4859/6206"),
                createUrlButton("Повышение уровня", "https://t.me/c/1856208477/4859/6206")
        ));
        rowList.add(List.of(
                createUrlButton("Лига X5Exp", "https://t.me/c/1856208477/4859/6208"),
                createUrlButton("Персонажи лиги", "https://t.me/c/1856208477/912/7775")
        ));
        rowList.add(List.of(
                createUrlButton("Куда тратить заработанные деньги?", "https://t.me/c/1856208477/918/920")
        ));

        rowList.add(List.of(createTextButton("Все команды", "help")));
        rowList.add(List.of(createTextButton("Назад", "sinfo")));
    }

    private void createSearchGameRuleButtons(final List<List<InlineKeyboardButton>> rowList) {
        rowList.add(List.of(
                createUrlButton("Запись", "https://t.me/c/1856208477/4859/6209"),
                createUrlButton("Акутальные игры", "https://t.me/c/1856208477/930")
        ));
        rowList.add(List.of(
                createUrlButton("Формат offline", "https://t.me/c/1856208477/4859/6210"),
                createUrlButton("Формат online", "https://t.me/c/1856208477/4859/6211")
        ));
        rowList.add(List.of(createUrlButton("Мастерам", "https://t.me/c/1856208477/4859/6212")));

        rowList.add(List.of(createTextButton("Все команды", "help")));
        rowList.add(List.of(createTextButton("Назад", "sinfo")));
    }


}
