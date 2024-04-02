package ru.x5.dnd.telegrambot.config;

public enum StateMachineEvents {
    TEXT_INPUT,
    NEW_MEMBERS,
    UNKNOWN_COMMAND,
    COMMAND_ANNOUNCE;

    public final static String COMMAND_PREFIX = "COMMAND_";
}
