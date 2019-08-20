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

import java.lang.reflect.InvocationTargetException;

public class SafPlatformThread implements PlatformThread {

    @Override
    public String getUserName() {
        try {
            return (String) Class.forName("com.ibm.os390.security.PlatformThread").getMethod("getUserName")
                    .invoke(null);

        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException | ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
