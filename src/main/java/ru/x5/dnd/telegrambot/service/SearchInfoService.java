package ru.x5.dnd.telegrambot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.x5.dnd.telegrambot.config.BotProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.x5.dnd.telegrambot.config.CallbackConstants.CALLBACK_COMMAND_SEARCH_INFO;
import static ru.x5.dnd.telegrambot.config.CallbackConstants.CALLBACK_SEARCH_ADMINISTRATOR;
import static ru.x5.dnd.telegrambot.config.CallbackConstants.CALLBACK_SEARCH_DND_5E_RULE;
import static ru.x5.dnd.telegrambot.config.CallbackConstants.CALLBACK_SEARCH_GAME_RULE;

import static ru.x5.dnd.telegrambot.config.CallbackConstants.CALLBACK_SEARCH_MECHANIC_TYPES;
import static ru.x5.dnd.telegrambot.utils.ButtonUtils.createTextButton;
import static ru.x5.dnd.telegrambot.utils.ButtonUtils.createUrlButton;

@Service
public class SearchInfoService implements InitializingBean {
    private final static Logger LOG = LoggerFactory.getLogger(SearchInfoService.class);

    private final TelegramService telegramService;
    private final PropertyMessageService propertyMessageService;

    private final BotProperties botProperties;

    private static final Map<String, String> MESSAGE_MAP = new HashMap<>();

    public SearchInfoService(TelegramService telegramService,
                             PropertyMessageService propertyMessageService,
                             BotProperties botProperties) {
        this.telegramService = telegramService;
        this.propertyMessageService = propertyMessageService;
        this.botProperties = botProperties;
    }

    /**
     * Отображение всех доступных команд поиска
     *
     * @param message сообщение
     */
    public void answer(final Message message) {
        var chatId = message.getChatId();
        var msg = prepareSendMessage(chatId);
        var isAdmin = botProperties.masters().contains(message.getFrom().getUserName());

        if (message.getChat().isUserChat()) {
            msg.setText(MESSAGE_MAP.get(CALLBACK_COMMAND_SEARCH_INFO));
            addButtons(msg, isAdmin);
        } else {
            msg.setText(propertyMessageService.getMessageByNameWithParams(
                    "search.write.me.private",
                    new Object[] { message.getFrom().getUserName() }
            ));
        }

        sendMessageToTelegram(msg, chatId);
    }

    /**
     * Отправка сообщения пользователю в чат при событии: "нажата кнопка"
     *
     * @param message сообщение
     * @param command событие машины состояний
     */
    public void callBackAnswer(final Message message, final String command) {
        var chatId = message.getChatId();
        var msg = prepareSendMessage(chatId);
        var isAdmin = botProperties.masters().contains(message.getChat().getUserName());

        if (message.getChat().isUserChat()) {
            msg.setText(MESSAGE_MAP.getOrDefault(command, CALLBACK_COMMAND_SEARCH_INFO));
            addCallBackButtons(command, msg, isAdmin);
        } else {
            msg.setText(propertyMessageService.getMessageByNameWithParams(
                    "search.write.me.private",
                    new Object[] { message.getFrom().getUserName() }
            ));
        }

        sendMessageToTelegram(msg, chatId);
    }

    private void sendMessageToTelegram(final SendMessage message, final Long chatId) {
        try {
            telegramService.execute(message);
        } catch (TelegramApiException e) {
            LOG.error("Cannot send message for chat id: {}", chatId, e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        fillMessageMap();
    }

    private SendMessage prepareSendMessage(final Long chatId) {
        var msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setParseMode(ParseMode.HTML);
        return msg;
    }

    /**
     * Создание кнопок
     *
     * @param msg отправлемое сообщение в чат
     */
    private void addButtons(final SendMessage msg, final boolean isAdmin) {
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        createDefaultSearchButtons(rowList, isAdmin);

        msg.setReplyMarkup(InlineKeyboardMarkup.builder()
                .keyboard(rowList)
                .build()
        );
    }
    private void addCallBackButtons(final String command, final SendMessage msg, final boolean isAdmin) {
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        if (CALLBACK_SEARCH_GAME_RULE.equals(command)) {
            createSearchGameRuleButtons(rowList);
        } else if (CALLBACK_SEARCH_DND_5E_RULE.equals(command)) {
            createSearchDNDRuleButtons(rowList);
        } else if (CALLBACK_SEARCH_MECHANIC_TYPES.equals(command)) {
            createMechanicTypesButtons(rowList);
        } else if (CALLBACK_SEARCH_ADMINISTRATOR.equals(command)) {
            createSearchAdministratorButtons(rowList);
        } else {
            createDefaultSearchButtons(rowList, isAdmin);
        }

        msg.setReplyMarkup(InlineKeyboardMarkup.builder()
                .keyboard(rowList)
                .build()
        );
    }

    /**
     * Отображение раздела поиска по-умолчанию
     *
     * @param rowList список кнопок
     * @param isAdmin признак того, что пользователь администратор
     */
    private void createDefaultSearchButtons(final List<List<InlineKeyboardButton>> rowList,
                                            final boolean isAdmin) {
        rowList.add(List.of(createTextButton("Правила механик", CALLBACK_SEARCH_MECHANIC_TYPES)));
        rowList.add(List.of(createTextButton("Информация по играм", CALLBACK_SEARCH_GAME_RULE)));

        if (isAdmin) {
            rowList.add(List.of(createTextButton("Команды администратора", CALLBACK_SEARCH_ADMINISTRATOR)));
        }

        rowList.add(List.of(createTextButton("Все команды", CALLBACK_COMMAND_SEARCH_INFO)));

        // используется для тестирования приветсвенного сообщения
        //rowList.add(List.of(createTextButton("greeting.new-member", "help.test")));
    }

    /**
     * Отображение раздела с механиками игры: "Поиск -> Правила механик"
     *
     * @param rowList список кнопок
     */
    private void createMechanicTypesButtons(final List<List<InlineKeyboardButton>> rowList) {
        rowList.add(List.of(createTextButton("D&D 5E", CALLBACK_SEARCH_DND_5E_RULE)));
        rowList.add(List.of(createUrlButton("Dungeon World", "https://t.me/c/1856208477/11034")));
        rowList.add(List.of(createUrlButton("Savage Worlds", "https://t.me/c/1856208477/14870")));

        rowList.add(List.of(createTextButton("Все команды", CALLBACK_COMMAND_SEARCH_INFO)));
    }

    /**
     * Отображение раздела с механикой: "Поиск -> Правила механик" -> "Правила D&D 5E"
     *
     * @param rowList список кнопок
     */
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

        rowList.add(List.of(createTextButton("Все команды", CALLBACK_COMMAND_SEARCH_INFO)));
    }

    /**
     * Отображение раздела: "Поиск -> Информация по играм"
     *
     * @param rowList список кнопок
     */
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

        rowList.add(List.of(createTextButton("Все команды", CALLBACK_COMMAND_SEARCH_INFO)));
    }

    /**
     * Отображение раздела: "Поиск -> Команды администратора"
     *
     * @param rowList список кнопок
     */
    private void createSearchAdministratorButtons(final List<List<InlineKeyboardButton>> rowList) {
        rowList.add(List.of(createTextButton("Все команды", CALLBACK_COMMAND_SEARCH_INFO)));
    }

    private void fillMessageMap() {
        LOG.info("Prepare message map by commands for 'SearchInfoService'");

        MESSAGE_MAP.put(CALLBACK_COMMAND_SEARCH_INFO, propertyMessageService.getMessageByName("search.info.help"));
        MESSAGE_MAP.put(CALLBACK_SEARCH_DND_5E_RULE, propertyMessageService.getMessageByName("search.dnd.5e.rule"));
        MESSAGE_MAP.put(CALLBACK_SEARCH_GAME_RULE, propertyMessageService.getMessageByName("search.game.info"));
        MESSAGE_MAP.put(CALLBACK_SEARCH_MECHANIC_TYPES, propertyMessageService.getMessageByName("search.mechanic.types"));

        MESSAGE_MAP.put(CALLBACK_SEARCH_ADMINISTRATOR, propertyMessageService.getMessageByName("search.administrator"));

        // используется для быстрйо отладки сообщений
        //MESSAGE_MAP.put("help.test", propertyMessageService.getMessageByNameWithParams("greeting.new-member", new Object[] { "@alexwardrune", botProperties.username() }));

        LOG.info("Prepare message map by commands - done\n");
    }
}
