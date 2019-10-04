/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
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
