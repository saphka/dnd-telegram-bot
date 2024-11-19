package ru.x5.dnd.telegrambot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;
import ru.x5.dnd.telegrambot.exception.MessagePropertiesException;

import java.util.Locale;

/**
 * Сервис чтения свойств с сообщениями из файла message.properties
 */
@Service
public class PropertyMessageService {
    private static final String GET_PROP_BY_NAME = "Get message from message.properties by name: {}";
    private static final String GET_PROP_BY_NAME_WITH_ARGS = "Get message from message.properties by name: {}, with args: {}";
    private final static Logger LOG = LoggerFactory.getLogger(PropertyMessageService.class);
    private final MessageSource messageSource;

    public PropertyMessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Чтение сообщениями из файла <b>message.properties</b> по названию свойства
     *
     * @param propertyName название свойства
     * @return сообщение
     */
    public String getMessageByName(final String propertyName) {
        LOG.debug(GET_PROP_BY_NAME, propertyName);
        return getPropertyByName(propertyName, null);
    }

    /**
     * Чтение сообщениями из файла <b>message.properties</b> по названию свойства,
     * сполседующим добавление параметров в текст
     *
     * @param propertyName название свойства
     * @param args параметры, добавляемые в сообщение
     * @return сообщение
     */
    public String getMessageByNameWithParams(final String propertyName, final Object[] args) {
        LOG.debug(GET_PROP_BY_NAME_WITH_ARGS, propertyName, args);
        return getPropertyByName(propertyName, args);
    }

    private String getPropertyByName(final String propertyName, final Object[] args) {
        try {
            return messageSource.getMessage(propertyName, args, Locale.getDefault());
        } catch (NoSuchMessageException ex) {
            throw new MessagePropertiesException(ex.getMessage(), ex.getCause());
        }
    }
}
