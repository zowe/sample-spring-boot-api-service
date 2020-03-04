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

import java.lang.reflect.InvocationTargetException;

public class SafPlatformThread implements PlatformThread {

    @Override
    public String getUserName() {
        try {
            return (String) Class.forName("com.ibm.os390.security.PlatformThread").getMethod("getUserName")
                    .invoke(null);

        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException | ClassNotFoundException e) {
            throw new SafPlatformError(e.getMessage(), e);
        }
    }
}
