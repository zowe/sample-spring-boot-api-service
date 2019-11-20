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

import java.util.List;
import java.util.Locale;

import org.zowe.commons.rest.response.ApiMessage;

/**
 * Service for creating {@link ApiMessage} by string key and list of parameters.
 * See default implementation {@link org.zowe.commons.error.ErrorServiceImpl}.
 */
public interface ErrorService {
    /**
     * Create {@link ApiMessage} that contains list of
     * {@link org.zowe.commons.rest.response.Message} with same key and provided
     * parameters.
     *
     * @param key        Key of message in messages.yml file.
     * @param parameters A list of parameters that will be used for formatting.
     *
     * @return {@link ApiMessage} for key
     */
    ApiMessage createApiMessage(String key, Object... parameters);

    /**
     * Create {@link ApiMessage} that contains list of
     * {@link org.zowe.commons.rest.response.Message} with same key and provided
     * parameters.
     *
     * @param key        Key of message in messages.yml file.
     * @param parameters A list that contains arrays of parameters that will be used
     *                   for formatting.
     * @return {@link ApiMessage} for key
     */
    ApiMessage createApiMessage(String key, List<Object[]> parameters);

    /**
     * Create {@link ApiMessage} that contains list of
     * {@link org.zowe.commons.rest.response.Message} with same key and provided
     * parameters.
     *
     * @param locale     The locale that is used to retrieve the localized message.
     *                   If it is null or message key is not found then the fallback
     *                   is to use the US English message.
     * @param key        Key of message in messages.yml file.
     * @param parameters A list of parameters that will be used for formatting.
     * @return {@link ApiMessage} for key
     */
    ApiMessage createApiMessage(Locale locale, String key, Object... parameters);

    /**
     * Create {@link ApiMessage} that contains list of
     * {@link org.zowe.commons.rest.response.Message} with same key and provided
     * parameters.
     *
     * @param locale     The locale that is used to retrieve the localized message.
     *                   If it is null or message key is not found then the fallback
     *                   is to use the US English message.
     * @param key        Key of message in messages.yml file.
     * @param parameters A list that contains arrays of parameters that will be used
     *                   for formatting.
     * @return {@link ApiMessage} for key
     */
    ApiMessage createApiMessage(Locale locale, String key, List<Object[]> parameters);

    /**
     * Loads messages to the context from the provided message file path.
     *
     * @param messagesFilePath Path of the message file resource.
     */
    void loadMessages(String messagesFilePath);

    /**
     * Loads localized messages from the provided resource bundle.
     *
     * @param baseName The base name of the resource bundle, a fully qualified class
     *                 name.
     */
    void addResourceBundleBaseName(String baseName);

    /**
     * Returns the message in the format that can be printed to console as a single
     * line or displayed to the user.
     *
     * @param key        Message key to be retrieved.
     * @param parameters Positional parameters require by the message.
     * @return Readable text for the given message key.
     */
    String getReadableMessage(String key, Object... parameters);

    /**
     * @return Returns the value of the default message source attribute that is
     *         added to every error message. It is a string that identifies the
     *         source service. For example: myhost:8080:serviceid
     */
    String getDefaultMessageSource();

    /**
     * @param defaultMessageSource The value of the default message source attribute
     *                             that is added to every error message. It is a
     *                             string that identifies the source service. For
     *                             example: myhost:8080:serviceid
     */
    void setDefaultMessageSource(String defaultMessageSource);
}
