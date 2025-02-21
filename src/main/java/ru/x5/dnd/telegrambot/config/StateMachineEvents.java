package ru.x5.dnd.telegrambot.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum StateMachineEvents {
    TEXT_INPUT(Set.of("")),
    NEW_MEMBERS(Set.of("")),
    COMMAND_ANNOUNCE(Set.of("")),
    COMMAND_ECHO(Set.of("")),
    COMMAND_STATS(Set.of("")),
    COMMAND_SEARCH(Set.of("")),

    /** Команда, отображающая стартовую информацию перед поиском */
    COMMAND_SEARCH_INFO(Set.of("sinfo", "searchinfo")),

    /** Команда, отображающая информацию по D&D */
    COMMAND_SEARCH_DND_5E_RULE(Set.of("sdnd5erule", "searchdnd5erule")),

    /** Команда, отображающая информацию по правилам проведения игр */
    COMMAND_SEARCH_GAME_RULE(Set.of("searchgamerule", "sgamerule", "sgrule")),

    /** Команда, отображающая информацию по всем доступным коммандам */
    COMMAND_HELP(Set.of("help")),

    CALLBACK_ANNOUNCE(Set.of("/announce")),

    UNKNOWN(Set.of(""))
    ;

    public final static String COMMAND_PREFIX = "COMMAND_";
    public final static String CALLBACK_PREFIX = "CALLBACK_";

    private final Set<String> commands;

    private static final Map<String, StateMachineEvents> CACHED_VALUES = new HashMap<>();

    static {
        for (StateMachineEvents items : values()) {
            items.commands.forEach(command -> CACHED_VALUES.put(command, items));
        }
    }

    StateMachineEvents(Set<String> commands) {
        this.commands = commands;
    }

    /**
     * Получение события по команде в виде строки
     *
     * @param stringCommand комманда в виде строки, переданная через БОТ
     * @return событие, если команда был найдена, иначе UNKNOWN
     */
    public static StateMachineEvents getStateEventByCommand(final String stringCommand) {
        return CACHED_VALUES.getOrDefault(stringCommand, StateMachineEvents.UNKNOWN);
    }
}
