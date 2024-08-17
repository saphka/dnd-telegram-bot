package ru.x5.dnd.telegrambot.config;

public class CallbackConstants {
    public static final String CALLBACK_SEPARATOR = ".";

    public static final String CALLBACK_ANNOUNCE = StateMachineEvents.CALLBACK_ANNOUNCE.name().substring(StateMachineEvents.CALLBACK_PREFIX.length()).toLowerCase();

    public static final String CALLBACK_ANNOUNCE_REG_WITH_CHARACTER = CALLBACK_ANNOUNCE + CALLBACK_SEPARATOR + "reg-char";
    public static final String CALLBACK_ANNOUNCE_REG_WITH_PREGEN = CALLBACK_ANNOUNCE + CALLBACK_SEPARATOR + "reg-pregen";
    public static final String CALLBACK_ANNOUNCE_REG_CANCEL = CALLBACK_ANNOUNCE + CALLBACK_SEPARATOR + "reg-cancel";

    private CallbackConstants() {
    }
}
