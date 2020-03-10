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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;

import org.zowe.commons.rest.response.MessageType;

import org.junit.Test;

public class ErrorMessageStorageTest {
    private final ErrorMessageStorage errorMessageStorage = new ErrorMessageStorage();

    @Test
    public void getKeyTest() {
        ErrorMessages messages = new ErrorMessages(
                Collections.singletonList(new ErrorMessage("key", "number", MessageType.ERROR, "error message")));

        errorMessageStorage.addMessages(messages);
        ErrorMessage notExistingKeyMessage = errorMessageStorage.getErrorMessage("some key");
        ErrorMessage existingKeyMessage = errorMessageStorage.getErrorMessage("key");

        assertNull(notExistingKeyMessage);
        assertEquals("key", existingKeyMessage.getKey());
        assertEquals("number", existingKeyMessage.getNumber());
        assertEquals("error message", existingKeyMessage.getText());
    }

    @Test(expected = DuplicateMessageException.class)
    public void addDuplicatedKeyMessages() {
        ErrorMessages messages = new ErrorMessages(
                Arrays.asList(new ErrorMessage("key", "number1", MessageType.ERROR, "error message"),
                        new ErrorMessage("key", "number2", MessageType.ERROR, "error message")));

        errorMessageStorage.addMessages(messages);
    }

    @Test(expected = DuplicateMessageException.class)
    public void addDuplicatedNumberMessages() {
        ErrorMessages messages = new ErrorMessages(
                Arrays.asList(new ErrorMessage("key1", "number", MessageType.ERROR, "error message"),
                        new ErrorMessage("key2", "number", MessageType.ERROR, "error message")));

        errorMessageStorage.addMessages(messages);
    }

}
