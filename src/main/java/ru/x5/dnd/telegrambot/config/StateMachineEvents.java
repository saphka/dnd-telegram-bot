package ru.x5.dnd.telegrambot.config;

public enum StateMachineEvents {
    TEXT_INPUT,
    NEW_MEMBERS,
    COMMAND_ANNOUNCE,
    COMMAND_ECHO,

    CALLBACK_ANNOUNCE,

    UNKNOWN;

    public final static String COMMAND_PREFIX = "COMMAND_";
    public final static String CALLBACK_PREFIX = "CALLBACK_";
}
