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

import java.util.ArrayList;
import java.util.List;

public class ErrorMessages {
    private List<ErrorMessage> messages;

    public ErrorMessages(List<ErrorMessage> messages) {
        this.messages = messages;
    }

    public ErrorMessages() {
        this.messages = new ArrayList<>();
    }

    public List<ErrorMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ErrorMessage> messages) {
        this.messages = messages;
    }
}
