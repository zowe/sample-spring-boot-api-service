/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.zos.security.service;

import org.zowe.commons.zos.security.platform.PlatformReturned;

public class AccessControlError extends RuntimeException {
    private static final long serialVersionUID = -101853226410917728L;
    private final PlatformReturned platformReturned;

    public AccessControlError(String message, PlatformReturned platformReturned) {
        super(message);
        this.platformReturned = platformReturned;
    }

    public PlatformReturned getPlatformReturned() {
        return platformReturned;
    }
}
