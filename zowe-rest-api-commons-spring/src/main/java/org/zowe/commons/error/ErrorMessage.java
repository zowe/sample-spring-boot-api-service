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

import org.zowe.commons.rest.response.MessageType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorMessage {
    private String key;
    private String number;
    private MessageType type;
    private String text;
    private String reason;
    private String action;
    private String component;

    public ErrorMessage(String key, String number, MessageType type, String text) {
       this(key, number, type, text, null, null, null);
    }
}
