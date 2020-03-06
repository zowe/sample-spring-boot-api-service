/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.rest.response;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Holds information about a {@link Message}. This class is immutable.
 * <p>
 * Example:
 *
 * <pre>
 * {
 *     &#64;code
 *     Message message = new BasicMessage(MessageType.ERROR, "MAS0001", "Message text.");
 * }
 * </pre>
 *
 * @author Greg Berres, Petr Plavjanik
 */
@JsonPropertyOrder({ "messageType", "messageNumber", "messageContent", "messageReason", "messageAction", "messageKey", "messageParameters", "messageInstanceId", "messageComponent", "messageSource"})
public class BasicMessage implements Message {
    private final MessageType messageType;
    private final String messageNumber;
    private final String messageContent;
    private final String messageSource;
    private final String messageReason;
    private final String messageAction;
    private final String messageKey;
    private final String messageInstanceId;
    private final List<Object> messageParameters;
    private final String messageComponent;

    @SuppressWarnings("squid:S00107")
    @JsonCreator
    public BasicMessage(@JsonProperty("messageType") MessageType messageType,
            @JsonProperty("messageNumber") String messageNumber, @JsonProperty("messageContent") String messageContent,
            @JsonProperty("messageReason") String messageReason, @JsonProperty("messageAction") String messageAction,
            @JsonProperty("messageKey") String messageKey,
            @JsonProperty("messageParameters") List<Object> messageParameters,
            @JsonProperty("messageInstanceId") String messageInstanceId,
            @JsonProperty("messageSource") String messageSource,
            @JsonProperty("messageComponent") String messageComponent) {
        this.messageType = messageType;
        this.messageNumber = messageNumber;
        this.messageContent = messageContent;
        this.messageReason = messageReason;
        this.messageAction = messageAction;
        this.messageKey = messageKey;
        this.messageParameters = messageParameters;
        this.messageInstanceId = messageInstanceId;
        this.messageSource = messageSource;
        this.messageComponent = messageComponent;
    }

    @SuppressWarnings("WeakerAccess")
    public BasicMessage(@JsonProperty("messageType") MessageType messageType,
            @JsonProperty("messageNumber") String messageNumber,
            @JsonProperty("messageContent") String messageContent) {
        this(messageType, messageNumber, messageContent, null, null, null, null, generateMessageInstanceId(), null, null);
    }

    @SuppressWarnings("WeakerAccess")
    public BasicMessage(@JsonProperty("messageKey") String messageKey,
            @JsonProperty("messageType") MessageType messageType, @JsonProperty("messageNumber") String messageNumber,
            @JsonProperty("messageContent") String messageContent) {
        this(messageType, messageNumber, messageContent, null, null, messageKey, null, generateMessageInstanceId(), null, null);
    }

    public static String generateMessageInstanceId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public MessageType getMessageType() {
        return messageType;
    }

    @Override
    public String getMessageContent() {
        return messageContent;
    }

    @Override
    public String getMessageNumber() {
        return messageNumber;
    }

    @Override
    public String getMessageSource() {
        return messageSource;
    }

    @Override
    public String getMessageReason() {
        return messageReason;
    }

    @Override
    public String getMessageAction() {
        return messageAction;
    }

    @Override
    public String getMessageKey() {
        return messageKey;
    }

    @Override
    public List<Object> getMessageParameters() {
        return messageParameters;
    }

    @Override
    public String getMessageInstanceId() {
        return messageInstanceId;
    }

    @Override
    public String getMessageComponent() {
        return messageComponent;
    }

    @Override
    public String toReadableText() {
        return getMessageNumber() + getMessageType().toChar() + ' ' + getMessageContent();
    }

    @Override
    @SuppressWarnings("squid:S1067")
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicMessage that = (BasicMessage) o;
        return messageType == that.messageType && Objects.equals(messageNumber, that.messageNumber)
                && Objects.equals(messageContent, that.messageContent)
                && Objects.equals(messageSource, that.messageSource)
                && Objects.equals(messageReason, that.messageReason)
                && Objects.equals(messageAction, that.messageAction) && Objects.equals(messageKey, that.messageKey)
                && Objects.equals(messageInstanceId, that.messageInstanceId)
                && Objects.equals(messageParameters, that.messageParameters)
                && Objects.equals(messageComponent, that.messageComponent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageType, messageNumber, messageContent, messageSource, messageReason, messageAction,
                messageKey, messageInstanceId, messageParameters, messageComponent);
    }

    @Override
    public String toString() {
        return "BasicMessage{" + "messageType=" + messageType + ", messageNumber='" + messageNumber + '\''
                + ", messageContent='" + messageContent + '\'' + ", messageSource='" + messageSource + '\''
                + ", messageReason='" + messageReason + '\'' + ", messageAction='" + messageAction + '\''
                + ", messageKey='" + messageKey + '\'' + ", messageInstanceId='" + messageInstanceId + '\''
                + ", messageParameters=" + messageParameters + ", messageComponent='" + messageComponent + '\'' + '}';
    }
}
