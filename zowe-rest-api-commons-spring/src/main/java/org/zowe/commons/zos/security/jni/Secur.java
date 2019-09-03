/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.zos.security.jni;

import static org.zowe.commons.zos.CommonsNativeLibraries.SECUR_LIBRARY_NAME;

import org.zowe.commons.zos.LibLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Secur {
    public Secur() {
        new LibLoader().loadLibrary(SECUR_LIBRARY_NAME);
    }

    public native int createSecurityEnvironment(String userid, String password, String applId);

    public native int createSecurityEnvironmentByDaemon(String userid, String applId);

    public native int removeSecurityEnvironment();
}
