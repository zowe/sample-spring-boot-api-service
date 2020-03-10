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

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * This interface is intended for REST API responses that contain error,
 * warning, or informational messages in the common Zowe format.
 *
 * It is preferred to return successful responses without messages if possible
 * and use only plain responses without wrapping for them.
 *
 * The {@link ApiMessage} and its implementation
 * {@link org.zowe.commons.rest.response.BasicApiMessage} should be used in the
 * case when a problem (an error) happens and then the response contains only
 * the error(s).
 *
 * When a response needs to contain both data and messages (e.g. warnings) then
 * it is advised for the response class to implement {@link ApiMessage} too. But
 * this should be an exception and we should try to make the REST API easy to
 * use without the need for the API user to process informational and warning
 * messages.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public interface ApiMessage {
    /**
     * A list of messages that contain error, warning, and informational content.
     */
    List<Message> getMessages();

    /**
     * Returns the first message in the format that can be printed to console as a
     * single line or displayed to the user.
     */
    String toReadableText();

    /**
     * The message that can be used in the server log. Includes the message instance
     * ID.
     */
    String toLogMessage();
}
