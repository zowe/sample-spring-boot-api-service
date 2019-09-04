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

    @Getter private final String module;
    @Getter private final int function;
    @Getter private final int returnValue;
    @Getter private final int returnCode;
    @Getter private final int reasonCode;

    public SecurityRequestFailed(String module, int function, int returnValue, int returnCode, int reasonCode,
            Throwable cause) {
        super(String.format(
                "Plaftorm security request has failed: function=%d, returnValue=%d, returnCode=%d, reasonCode=0x%04X",
                function, returnValue, returnCode, reasonCode, cause));
        this.module = module;
        this.function = function;
        this.returnValue = returnValue;
        this.returnCode = returnCode;
        this.reasonCode = reasonCode;
    }
}
