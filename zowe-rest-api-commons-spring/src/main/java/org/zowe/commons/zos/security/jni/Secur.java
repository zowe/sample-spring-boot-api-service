/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
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

    public native int getLastErrno2();

    public native int setApplid(String applid);
}
