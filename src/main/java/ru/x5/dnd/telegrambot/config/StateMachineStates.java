package ru.x5.dnd.telegrambot.config;

public enum StateMachineStates {
    READY,
    ECHO,
    GREET_NEW_MEMBERS,
    GET_GAME_STATS,
    SEARCH,

    /** Событие, отображающее стартовую информацию перед поиском */
    SEARCH_INFO,

    /** Событие, отображающее информацию по D&D */
    SEARCH_DND_5E_RULE,

    /** Событие, отображающее информацию по правилам проведения игр */
    SEARCH_GAME_RULE,

    /** Событие, отображающее информацию по всем доступным коммандам */
    HELP,

    ANNOUNCE_GAME,
    ANNOUNCE_CALLBACK
}
