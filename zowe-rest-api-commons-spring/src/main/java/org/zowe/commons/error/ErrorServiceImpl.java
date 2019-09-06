/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.error;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.IllegalFormatConversionException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.zowe.commons.rest.response.ApiMessage;
import org.zowe.commons.rest.response.BasicApiMessage;
import org.zowe.commons.rest.response.BasicMessage;
import org.zowe.commons.rest.response.Message;
import org.zowe.commons.rest.response.MessageType;

/**
 * Default implementation of {@link ErrorService} that uses messages.yml as
 * source for messages.
 */
public class ErrorServiceImpl implements ErrorService {
    private static final String COMMONS_MESSAGES = "/commons-messages.yml";
    private static final String INVALID_KEY_MESSAGE = "org.zowe.commons.error.invalidMessageKey";
    private static final String INVALID_MESSAGE_TEXT_FORMAT = "org.zowe.commons.error.invalidMessageTextFormat";
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorServiceImpl.class);

    private final ErrorMessageStorage messageStorage;

    /**
     * Constructor that creates only common messages.
     */
    public ErrorServiceImpl() {
        messageStorage = new ErrorMessageStorage();
        loadMessages(COMMONS_MESSAGES);
    }

    /**
     * Constructor that creates common messages and messages from file.
     *
     * @param messagesFilePath path to file with messages.
     */
    public ErrorServiceImpl(String messagesFilePath) {
        this();
        loadMessages(messagesFilePath);
    }

    /**
     * Creates {@link ApiMessage} with key and list of parameters.
     *
     * @param key        of message in messages.yml file
     * @param parameters for message
     * @return {@link ApiMessage}
     */
    @Override
    public ApiMessage createApiMessage(String key, Object... parameters) {
        Message message = createMessage(key, parameters);
        return new BasicApiMessage(Collections.singletonList(message));
    }

    /**
     * Creates {@link ApiMessage} with list of {@link Message}.
     *
     * @param key        of message in messages.yml file
     * @param parameters list that contains arrays of parameters
     * @return {@link ApiMessage}
     */
    @Override
    public ApiMessage createApiMessage(String key, List<Object[]> parameters) {
        List<Message> messageList = parameters.stream().filter(Objects::nonNull).map(ob -> createMessage(key, ob))
                .collect(Collectors.toList());
        return new BasicApiMessage(messageList);
    }

    /**
     * Load messages to the context from the provided message file path
     *
     * @param messagesFilePath path of the message file
     * @throws MessageLoadException      when a message couldn't loaded or has wrong
     *                                   definition
     * @throws DuplicateMessageException when a message is already defined before
     */
    public void loadMessages(String messagesFilePath) {
        try (InputStream in = ErrorServiceImpl.class.getResourceAsStream(messagesFilePath)) {
            Yaml yaml = new Yaml();
            ErrorMessages applicationMessages = yaml.loadAs(in, ErrorMessages.class);
            messageStorage.addMessages(applicationMessages);
        } catch (YAMLException | IOException e) {
            throw new MessageLoadException(
                    "There is problem with reading application messages file: " + messagesFilePath, e);
        }
    }

    /**
     * Internal method that call {@link ErrorMessageStorage} to get message by key.
     *
     * @param key        of message.
     * @param parameters array of parameters for message.
     * @return {@link Message} in mainframe format
     */
    private Message createMessage(String key, Object... parameters) {
        ErrorMessage message = messageStorage.getErrorMessage(key);
        message = validateMessage(message, key);
        Object[] messageParameters = validateParameters(message, key, parameters);

        String text;
        try {
            text = String.format(message.getText(), messageParameters);
        } catch (IllegalFormatConversionException exception) {
            LOGGER.debug("Internal error: Invalid message format was used", exception);
            message = messageStorage.getErrorMessage(INVALID_MESSAGE_TEXT_FORMAT);
            message = validateMessage(message, key);
            messageParameters = validateParameters(message, key, parameters);
            text = String.format(message.getText(), messageParameters);
        }

        return new BasicMessage(key, message.getType(), message.getNumber(), text);
    }

    /**
     * Internal method that validates the message. When the message does not exist,
     * the key {@value INVALID_KEY_MESSAGE} is used. When this message also does not
     * exist, the new predefined message is created.
     *
     * @param message to be checked
     * @param key     of message
     * @return {@link ErrorMessage} in mainframe format
     */
    private ErrorMessage validateMessage(ErrorMessage message, String key) {
        if (message == null) {
            LOGGER.debug("Invalid message key '{}' was used. Please resolve this problem.", key);
            message = messageStorage.getErrorMessage(INVALID_KEY_MESSAGE);
        }

        if (message == null) {
            String text = "Internal error: Invalid message key '%s' provided. No default message found. Please contact support of further assistance.";
            message = new ErrorMessage(INVALID_KEY_MESSAGE, "ZWEAS001", MessageType.ERROR, text);
        }

        return message;
    }

    @Override
    public String getReadableMessage(String key, Object... parameters) {
        return createApiMessage(key, parameters).toReadableText();
    }

    /**
     * Internal method that modifies parameters when the original message key does
     * not exist and the new error message to indicate this issue is being used.
     *
     * @param message    to be checked if the original message was invalid
     * @param key        of the original message
     * @param parameters of the original message
     * @return modified parameters if the message was changed, otherwise parameters
     *         remain unchanged
     */
    private Object[] validateParameters(ErrorMessage message, String key, Object... parameters) {
        if (message.getKey().equals(INVALID_KEY_MESSAGE)) {
            return new Object[] { key };
        } else {
            return parameters;
        }
    }
}
