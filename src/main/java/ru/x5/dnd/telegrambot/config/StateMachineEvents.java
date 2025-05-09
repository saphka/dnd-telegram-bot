package ru.x5.dnd.telegrambot.config;

import java.util.HashMap;
import java.util.Map;

public enum StateMachineEvents {
    TEXT_INPUT(),
    NEW_MEMBERS(),
    COMMAND_ANNOUNCE(),
    COMMAND_ECHO(),
    COMMAND_STATS(),
    COMMAND_SEARCH(),
    
    /** Команда, отображающая информацию по всем доступным коммандам */
    COMMAND_HELP(),
    CALLBACK_HELP(),

    CALLBACK_ANNOUNCE(),

    UNKNOWN()
    ;

    public final static String COMMAND_PREFIX = "COMMAND_";
    public final static String CALLBACK_PREFIX = "CALLBACK_";

    private static final Map<String, StateMachineEvents> CACHED_VALUES = new HashMap<>();

    static {
        for (StateMachineEvents item : values()) {
            CACHED_VALUES.put(item.name(), item);
        }
    }

    /**
     * Получение события по команде в виде строки
     *
     * @param stringCommand комманда в виде строки, переданная через БОТ
     * @return событие, если команда был найдена, иначе UNKNOWN
     */
    public static StateMachineEvents getStateEventByCommand(final String stringCommand) {
        for (String key : CACHED_VALUES.keySet()) {
            if (key.equalsIgnoreCase(stringCommand)) {
                return CACHED_VALUES.get(key);
            }
        }
        return UNKNOWN;
    }
}
