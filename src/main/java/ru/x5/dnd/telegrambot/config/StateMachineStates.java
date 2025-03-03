package ru.x5.dnd.telegrambot.config;

public enum StateMachineStates {
    READY,
    ECHO,
    GREET_NEW_MEMBERS,
    GET_GAME_STATS,
    SEARCH,

    /** Событие, отображающее информацию по всем доступным коммандам */
    HELP,
    /** Событие, реагирующее на действия пользователя и администратора при поиске */
    HELP_CALLBACK,

    /** Событие, создающее анонс игры */
    ANNOUNCE_GAME,
    /** Событие, реагирующее на действия пользователя и администратора: запись, отмена и т.п. */
    ANNOUNCE_CALLBACK
}
