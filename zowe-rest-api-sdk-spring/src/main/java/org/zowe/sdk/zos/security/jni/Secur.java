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

public class Secur {
    public Secur() {
        System.loadLibrary(SECUR_LIBRARY_NAME);
    }

    public native int createSecurityEnvironment(String userid, String password, String applId);

    public native int createSecurityEnvironmentByDaemon(String userid, String applId);

    public native int removeSecurityEnvironment();
}
