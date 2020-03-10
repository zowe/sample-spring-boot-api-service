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

import lombok.Getter;

public class SecurityRequestFailed extends RuntimeException {
    private static final long serialVersionUID = 6832104396884487813L;

    @Getter
    private final String module;
    @Getter
    private final int function;
    @Getter
    private final int errno;

    public SecurityRequestFailed(String module, int function, int errno, Throwable cause) {
        super(String.format("Platform security request has failed: module=%s, function=%d, errno=%d", module, function,
                errno), cause);
        this.module = module;
        this.function = function;
        this.errno = errno;
    }

    public SecurityRequestFailed(String module, int function, int errno) {
        this(module, function, errno, null);
    }

}
