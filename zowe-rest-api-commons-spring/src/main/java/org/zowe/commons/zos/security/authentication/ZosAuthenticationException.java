/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.zos.security.authentication;

import org.springframework.security.core.AuthenticationException;
import org.zowe.commons.zos.security.platform.PlatformReturned;

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
