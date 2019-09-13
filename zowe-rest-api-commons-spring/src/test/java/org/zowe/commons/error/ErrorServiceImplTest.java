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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.zowe.commons.error.ErrorService;
import org.zowe.commons.rest.response.ApiMessage;

import org.junit.Test;

public class ErrorServiceImplTest {
    private final ErrorService errorService = new ErrorServiceImpl();

    @Test
    public void invalidMessageKey() {
        ApiMessage message = errorService.createApiMessage("nonExistingKey", "someParameter1", "someParameter2");

        assertEquals("ZWEAS001", message.getMessages().get(0).getMessageNumber());
        assertEquals(
                "Internal error: Invalid message key 'nonExistingKey' is provided. Please contact support for further assistance.",
                message.getMessages().get(0).getMessageContent());
    }

    @Test
    public void validMessageKey() {
        ApiMessage message = errorService.createApiMessage("org.zowe.commons.rest.unsupportedMediaType");

        assertEquals("ZWEAS415", message.getMessages().get(0).getMessageNumber());
    }

    @Test
    public void constructorWithExistingFile() {
        ErrorService errorServiceFromFile = new ErrorServiceImpl("/test-messages.yml");

        ApiMessage message = errorServiceFromFile.createApiMessage("org.zowe.commons.test.noArguments");

        assertEquals("CSC0001", message.getMessages().get(0).getMessageNumber());
        assertEquals("No arguments message", message.getMessages().get(0).getMessageContent());
    }

    @Test(expected = MessageLoadException.class)
    public void constructorWithNotExistingFile() {
        errorService.loadMessages("/some-not-existing-messages.yml");
        errorService.createApiMessage("org.zowe.commons.test.noArguments");
    }

    @Test
    public void createListOfMessagesWithSameKey() {
        Object[] parametersEntity1 = new Object[] { null };
        Object[] parametersEntity2 = new Object[] { null };
        List<Object[]> parameters = new ArrayList<>();
        parameters.add(parametersEntity1);
        parameters.add(parametersEntity2);
        ApiMessage message = errorService.createApiMessage("org.zowe.commons.rest.notFound", parameters);

        assertEquals("ZWEAS404", message.getMessages().get(0).getMessageNumber());
        assertEquals("The service can not find requested resource.", message.getMessages().get(0).getMessageContent());
    }

    @Test
    public void invalidMessageTextFormat() {
        errorService.loadMessages("/test-messages.yml");

        ApiMessage message = errorService.createApiMessage("org.zowe.commons.test.invalidParameterFormat", "test",
                "someParameter2");

        assertEquals("ZWEAS002", message.getMessages().get(0).getMessageNumber());
        assertEquals("Internal error: Invalid message text format. Please contact support for further assistance.",
                message.getMessages().get(0).getMessageContent());
    }

    @Test(expected = DuplicateMessageException.class)
    public void constructorWithDuplicatedMessages() {
        errorService.loadMessages("/test-duplicate-messages.yml");
        errorService.createApiMessage("org.zowe.commons.test.noArguments");
    }
}
