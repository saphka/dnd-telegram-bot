package ru.x5.dnd.telegrambot.config;

public class CallbackConstants {
    public static final String CALLBACK_SEPARATOR = ".";

    public static final String ANNOUNCE_PREFIX = StateMachineEvents.CALLBACK_ANNOUNCE.name().substring(StateMachineEvents.CALLBACK_PREFIX.length()).toLowerCase();
    public static final String HELP_PREFIX = StateMachineEvents.CALLBACK_HELP.name().substring(StateMachineEvents.CALLBACK_PREFIX.length()).toLowerCase();

    public static final String CALLBACK_ANNOUNCE_REG_WITH_CHARACTER = ANNOUNCE_PREFIX + CALLBACK_SEPARATOR + "reg-char";
    public static final String CALLBACK_ANNOUNCE_REG_WITH_PREGEN = ANNOUNCE_PREFIX + CALLBACK_SEPARATOR + "reg-pregen";
    public static final String CALLBACK_ANNOUNCE_REG_CANCEL = ANNOUNCE_PREFIX + CALLBACK_SEPARATOR + "reg-cancel";
    public static final String CALLBACK_ANNOUNCE_CANCEL = ANNOUNCE_PREFIX + CALLBACK_SEPARATOR + "cancel";

    public static final String CALLBACK_COMMAND_SEARCH_INFO = HELP_PREFIX + CALLBACK_SEPARATOR + "search-info";

    public static final String CALLBACK_SEARCH_MECHANIC_TYPES = HELP_PREFIX + CALLBACK_SEPARATOR + "search-mechanic-types";
    public static final String CALLBACK_SEARCH_DND_5E_RULE = HELP_PREFIX + CALLBACK_SEPARATOR + "search-dnd5e-rule";
    public static final String CALLBACK_SEARCH_GAME_RULE = HELP_PREFIX + CALLBACK_SEPARATOR + "search-game-rule";

    public static final String CALLBACK_SEARCH_ADMINISTRATOR = HELP_PREFIX + CALLBACK_SEPARATOR + "search-administrator";

    private CallbackConstants() {
    }
}
