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

/**
 * Exception thrown when a message couldn't be loaded or has wrong definition
 */
public class MessageLoadException extends RuntimeException {
    private static final long serialVersionUID = -1890955912059595381L;

    public MessageLoadException(String message, Throwable cause) {
        super(message, cause);
    }

}
