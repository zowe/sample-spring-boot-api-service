/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sdk.zos.security.service;

public class SecurityRequestFailed extends RuntimeException {
    private static final long serialVersionUID = 6832104396884487813L;

    private final String module;
    private final int function;
    private final int returnValue;
    private final int returnCode;
    private final int reasonCode;

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

    public int getReturnCode() {
        return returnCode;
    }

    public int getReasonCode() {
        return reasonCode;
    }

    public int getReturnValue() {
        return returnValue;
    }

    public int getFunction() {
        return function;
    }

    public String getModule() {
        return module;
    }
}
