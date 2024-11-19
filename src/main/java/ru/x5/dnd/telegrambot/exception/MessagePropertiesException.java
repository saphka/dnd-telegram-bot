package ru.x5.dnd.telegrambot.exception;

public class MessagePropertiesException extends RuntimeException {
    public MessagePropertiesException(String message) {
        super(message);
    }
    public MessagePropertiesException(String message, Throwable cause) {
        super(message, cause);
    }
}
