package ru.x5.dnd.telegrambot.utils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public final class ButtonUtils {
    /**
     * Содание <b>InlineKeyboardButton</b> типа ссылки
     *
     * @param text название кнопки
     * @param command комманда, которая будет выполнена по нажатию на кнопку
     * @return кнопка
     * @see InlineKeyboardButton
     */
    public static InlineKeyboardButton createTextButton(final String text, final String command) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(command)
                .build();
    }

    /**
     * Содание <b>InlineKeyboardButton</b> типа ссылки
     *
     * @param text название кнопки
     * @param url адрес, на который будет выполнен переход
     * @return кнопка
     * @see InlineKeyboardButton
     */
    public static InlineKeyboardButton createUrlButton(final String text, final String url) {
        return InlineKeyboardButton.builder()
                .text(text)
                .url(url)
                .build();
    }
}
