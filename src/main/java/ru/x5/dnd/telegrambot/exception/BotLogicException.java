package ru.x5.dnd.telegrambot.exception;

public class BotLogicException extends RuntimeException {
    public BotLogicException(String message) {
        super(message);
    }
    public BotLogicException(String message, Throwable cause) {
        super(message, cause);
    }
}
