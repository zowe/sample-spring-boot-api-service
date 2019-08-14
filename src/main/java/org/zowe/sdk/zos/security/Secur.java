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

public class Secur {
    private static final String LIBRARY_NAME = "Secur";

    Secur() {
        System.loadLibrary(LIBRARY_NAME);
    }

    // Native methods have to be in sync with
    // "zossrc/com/ca/mfaas/sampleservice/security/Secur.java"
    native int createSecurityEnvironment(String userid, String password, String applId);

    native int createSecurityEnvironmentByDaemon(String userid, String applId);

    native int removeSecurityEnvironment();

    public String getLibraryName() {
        return LIBRARY_NAME;
    }
}
