/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.zos.security.platform;

import java.lang.reflect.InvocationTargetException;

public class SafPlatformUser implements PlatformUser {
    private final PlatformClassFactory platformClassFactory;

    public SafPlatformUser(PlatformClassFactory platformClassFactory) {
        this.platformClassFactory = platformClassFactory;
    }

    @Override
    public PlatformReturned authenticate(String userid, String password) {
        if ((password == null) || password.isEmpty()) {
            return PlatformReturned.builder().success(false).rc(0).errno(PlatformPwdErrno.EINVAL.errno).errno2(PlatformErrno2.ERRNO2_BASE | PlatformErrno2.JRPasswordLenError.errno2).build();
        }
        try {
            Object safReturned = platformClassFactory.getPlatformUserClass()
                    .getMethod("authenticate", String.class, String.class)
                    .invoke(platformClassFactory.getPlatformUser(), userid, password);
            if (safReturned == null) {
                return null;
            } else {
                return platformClassFactory.convertPlatformReturned(safReturned);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException | ClassNotFoundException | NoSuchFieldException e) {
            throw new SafPlatformError(e.getMessage(), e);
        }
    }
}
