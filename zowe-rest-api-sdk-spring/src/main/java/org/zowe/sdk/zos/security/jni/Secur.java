/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sdk.zos.security.jni;

import static org.zowe.sdk.zos.SdkNativeLibraries.SECUR_LIBRARY_NAME;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Secur {
    public Secur() {
        try {
            System.loadLibrary(SECUR_LIBRARY_NAME);
        }
        catch (UnsatisfiedLinkError e) {
            log.info("java.library.path={}", System.getProperty("java.library.path"));
            throw e;
        }
    }

    public native int createSecurityEnvironment(String userid, String password, String applId);

    public native int createSecurityEnvironmentByDaemon(String userid, String applId);

    public native int removeSecurityEnvironment();
}
