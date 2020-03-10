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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.zowe.commons.rest.response.ApiMessage;

public class ErrorServiceImplTest {
    private final ErrorService errorService = ErrorServiceImpl.getCommonsDefault();

    @Test(expected = MessageLoadException.class)
    public void defaultErrorServiceFailsInCommons() {
        ErrorServiceImpl.getDefault();
    }

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
    public void validDefaultMessageComponent() {
        ApiMessage message = errorService.createApiMessage("org.zowe.commons.rest.unsupportedMediaType");

        assertEquals("org.zowe.commons.error.ErrorServiceImplTest", message.getMessages().get(0).getMessageComponent());
    }

    @Test
    public void validMessageComponent() {
        ErrorService errorServiceFromFile = new ErrorServiceImpl("/test-messages.yml");
        ApiMessage message = errorServiceFromFile.createApiMessage("org.zowe.commons.test.component");
        assertEquals("zowe.sdk.commons.test", message.getMessages().get(0).getMessageComponent());
    }

    @Test
    public void validLocalizedTexts() {
        ErrorService errorServiceFromFile = new ErrorServiceImpl("/test-messages.yml");
        errorServiceFromFile.addResourceBundleBaseName("test-messages");
        ApiMessage message = errorServiceFromFile.createApiMessage(Locale.forLanguageTag("cs-CZ"),
                "org.zowe.commons.test.localized");
        assertEquals("Lokalizovaná zpráva", message.getMessages().get(0).getMessageContent());
        assertEquals("Akce", message.getMessages().get(0).getMessageAction());
        assertEquals("Důvod", message.getMessages().get(0).getMessageReason());
        assertEquals("Komponenta", message.getMessages().get(0).getMessageComponent());
    }

    @Test
    public void validMessageSource() {
        ErrorService errorServiceFromFile = new ErrorServiceImpl("/test-messages.yml");
        errorServiceFromFile.setDefaultMessageSource("host:port:service");
        ApiMessage message = errorServiceFromFile.createApiMessage("org.zowe.commons.test.noArguments");
        assertEquals("host:port:service", message.getMessages().get(0).getMessageSource());
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

    @Test
    public void messageWithReason() {
        ErrorService errorServiceFromFile = new ErrorServiceImpl("/test-messages.yml");

        ApiMessage message = errorServiceFromFile.createApiMessage("org.zowe.commons.test.reason");

        assertEquals("CSC0002", message.getMessages().get(0).getMessageNumber());
        assertEquals("Reason", message.getMessages().get(0).getMessageReason());
    }

    @Test
    public void messageWithReasonParameters() {
        ErrorService errorServiceFromFile = new ErrorServiceImpl("/test-messages.yml");

        ApiMessage message = errorServiceFromFile.createApiMessage("org.zowe.commons.test.parameters", "string", 123);

        assertEquals("Test message - expects decimal number 123 and string",
                message.getMessages().get(0).getMessageContent());
        assertTrue(message.getMessages().get(0).getMessageParameters().contains(123));
        assertTrue(message.getMessages().get(0).getMessageParameters().contains("string"));
    }

    @Test
    public void messageWithAction() {
        ErrorService errorServiceFromFile = new ErrorServiceImpl("/test-messages.yml");

        ApiMessage message = errorServiceFromFile.createApiMessage("org.zowe.commons.test.action");

        assertEquals("CSC0003", message.getMessages().get(0).getMessageNumber());
        assertEquals("Action", message.getMessages().get(0).getMessageAction());
    }
}
