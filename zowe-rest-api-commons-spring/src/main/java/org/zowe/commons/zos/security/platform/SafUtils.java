/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.zos.security.platform;

import org.zowe.commons.zos.ZosUtils;
import org.zowe.commons.zos.security.jni.Secur;

public class SafUtils {
    private SafUtils() {
        // no instances
    }

    private static ThreadLocal<String> threadApplid = new ThreadLocal<>();

    /**
     * Sets the APPLID for the current thread so the PlatformUser.authenticate can
     * use PassTickets for the provide APPLID.
     *
     * The APPLID can be changed but not unset.
     *
     * @param applid The APPLID to be set. Up to 8 characters.
     */
    public static void setThreadApplid(String applid) {
        if ((applid == null) || applid.isEmpty()) {
            return;
        }

        String currentApplid = threadApplid.get();
        if (currentApplid == null || !currentApplid.equals(applid)) {
            if (ZosUtils.isRunningOnZos()) {
                new Secur().setApplid(applid);
            }
            threadApplid.set(applid);
        }
    }
}
