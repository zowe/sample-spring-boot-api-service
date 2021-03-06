/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.error;

import java.util.HashMap;
import java.util.Map;

/**
 * Error message storage class
 */
public class ErrorMessageStorage {
    private final Map<String, ErrorMessage> keyMap = new HashMap<>();
    private final Map<String, ErrorMessage> numberMap = new HashMap<>();

    /**
     * Retrieve error message from the storage by message key
     *
     * @param key Message Key
     * @return Error message
     */
    public ErrorMessage getErrorMessage(String key) {
        return keyMap.get(key);
    }

    /**
     * Method for adding messages to storage
     *
     * @param messages Error message
     */
    public void addMessages(ErrorMessages messages) {
        Map<String, ErrorMessage> currentKeyMap = new HashMap<>();
        for (ErrorMessage message : messages.getMessages()) {
            if (!currentKeyMap.containsKey(message.getKey())) {
                if (!numberMap.containsKey(message.getNumber())) {
                    currentKeyMap.put(message.getKey(), message);
                    keyMap.put(message.getKey(), message);
                    numberMap.put(message.getNumber(), message);
                } else {
                    String exceptionMessage = String.format("Message with number '%s' already exists",
                            message.getNumber());
                    throw new DuplicateMessageException(exceptionMessage);
                }
            } else {
                String exceptionMessage = String.format("Message with key '%s' already exists", message.getKey());
                throw new DuplicateMessageException(exceptionMessage);
            }
        }
    }
}
