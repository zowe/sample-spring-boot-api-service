/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sdk.zos.security;

import org.springframework.security.core.AuthenticationException;

public class ZosAuthenticationException extends AuthenticationException {

    private static final long serialVersionUID = 6652673387938170807L;
    private final PlatformReturned platformReturned;

    public ZosAuthenticationException(String message, PlatformReturned platformReturned) {
        super(message);
        this.platformReturned = platformReturned;
    }

    public PlatformReturned getPlatformReturned() {
        return platformReturned;
    }
}
