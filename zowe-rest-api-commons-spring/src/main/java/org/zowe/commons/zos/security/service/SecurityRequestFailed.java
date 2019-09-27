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
        super(String.format("Platform security request has failed: module=%s, function=%d, errno=%d", module, function,
                errno));
        this.module = module;
        this.function = function;
        this.errno = errno;
    }

}
