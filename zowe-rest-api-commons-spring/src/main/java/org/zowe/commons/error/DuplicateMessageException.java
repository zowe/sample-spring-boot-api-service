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
 * Exception thrown when a message is already defined before
 */
public class DuplicateMessageException extends RuntimeException {
    private static final long serialVersionUID = -3407108866724093071L;

    public DuplicateMessageException(String message) {
        super(message);
    }
}
