/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.zos.security.platform;

public class SafPlatformError extends RuntimeException {
    private static final long serialVersionUID = 1920433542069453114L;

    public SafPlatformError(Throwable e) {
        super(e);
	}

	public SafPlatformError(String message, Exception e) {
        super(message, e);
	}

	public SafPlatformError(String message) {
        super(message);
	}
}
