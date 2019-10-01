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

public class SafPlatformAccessControl implements PlatformAccessControl {
    private final PlatformClassFactory platformClassFactory;

    public SafPlatformAccessControl(PlatformClassFactory platformClassFactory) {
        this.platformClassFactory = platformClassFactory;
    }

    @Override
    public PlatformReturned checkPermission(String userid, String resourceClass, String resourceName,
            int accessLevel) {
        try {
            Object safReturned = platformClassFactory.getPlatformAccessControlClass()
                    .getMethod("checkPermission", String.class, String.class, String.class, int.class)
                    .invoke(platformClassFactory.getPlatformAccessControl(), userid, resourceClass, resourceName,
                            accessLevel);
            return platformClassFactory.convertPlatformReturned(safReturned);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException | ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public PlatformReturned checkPermission(String resourceClass, String resourceName, int accessLevel) {
        try {
            Object safReturned = platformClassFactory.getPlatformAccessControlClass()
                    .getMethod("checkPermission", String.class, String.class, int.class)
                    .invoke(platformClassFactory.getPlatformAccessControl(), resourceClass, resourceName,
                            accessLevel);
            return platformClassFactory.convertPlatformReturned(safReturned);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException | ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
