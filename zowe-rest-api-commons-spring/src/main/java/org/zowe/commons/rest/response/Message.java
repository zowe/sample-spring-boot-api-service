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

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * User facing messages that can be provided with API responses.
 *
 * We should include as much useful data as possible and keep in mind different
 * users of the message structure. However remember that one kind of user may be
 * a person with malicious intent so not leak data that should be kept private
 * or implementation details.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public interface Message {
    /**
     * The severity of a problem. This field is required.
     */
    MessageType getMessageType();

    /**
     * Typical mainframe message number (not including the message level one-letter
     * code) that can be found in typical mainframe documentation. The message
     * number is usually in this format "pppnnnn" where ppp is a product code and
     * nnnn is a four-digit number.
     *
     * Example: "PFI0031"
     */
    String getMessageNumber();

    /**
     * Readable message in US English. This field is required. It should be a
     * sentence starting with a capital letter and ending with a full stop (.).
     */
    String getMessageContent();

    /**
     * Supplements the messageContent field, supplying more information about why
     * the message is present. This field is optional.
     */
    String getMessageAction();

    /**
     * Recommendation of the actions to take in response to the message. This field
     * is optional.
     */
    String getMessageReason();

    /**
     * Optional unique key describing the reason of the error. It should be a dot
     * delimited string "com.ca.service[.subservice].detail". The purpose of this
     * field is to enable UI to show a meaningful and localized error message.
     */
    String getMessageKey();

    /**
     * Optional error message parameters. Used for formatting of localized messages.
     */
    List<Object> getMessageParameters();

    /**
     * Optional unique ID of the message instance. Useful for finding of the message
     * in the logs. The same ID should be printed in the log. This field is
     * optional.
     *
     * Example: "123e4567-e89b-12d3-a456-426655440000"
     */
    String getMessageInstanceId();

    /**
     * For support and developers - component that generated the error (can be fully
     * qualified Java package or class name). This field is optional.
     *
     * Example: org.zowe.service.package.Class
     */
    String getMessageComponent();

    /**
     * For support and developers - source service that generated the error (it can
     * be a Zowe service name or host:port). This field is optional.
     *
     * Example: zzow01.zowe.marist.cloud:1234:myservice
     */
    String getMessageSource();

    /**
     * Returns message in the format that can be printed to console as a single line
     * or displayed to the user.
     **/
    String toReadableText();
}
