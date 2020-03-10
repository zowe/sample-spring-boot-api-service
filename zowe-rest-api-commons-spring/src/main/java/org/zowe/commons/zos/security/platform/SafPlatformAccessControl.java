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

public class SafPlatformAccessControl implements PlatformAccessControl {
    private final PlatformClassFactory platformClassFactory;
    private Object platformAccessControl;

    public SafPlatformAccessControl(PlatformClassFactory platformClassFactory) {
        this.platformClassFactory = platformClassFactory;
        try {
			this.platformAccessControl = platformClassFactory.getPlatformAccessControl();
		} catch (ClassNotFoundException e) {
            throw new SafPlatformError(e);
		}
    }

    @Override
    public PlatformReturned checkPermission(String userid, String resourceClass, String resourceName,
            int accessLevel) {
        try {
            Object safReturned = platformClassFactory.getPlatformAccessControlClass()
                    .getMethod("checkPermission", String.class, String.class, String.class, int.class)
                    .invoke(platformAccessControl, userid, resourceClass, resourceName,
                            accessLevel);
            return platformClassFactory.convertPlatformReturned(safReturned);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException | ClassNotFoundException | NoSuchFieldException e) {
            throw new SafPlatformError(e.getMessage(), e);
        }
    }

    @Override
    public PlatformReturned checkPermission(String resourceClass, String resourceName, int accessLevel) {
        try {
            Object safReturned = platformClassFactory.getPlatformAccessControlClass()
                    .getMethod("checkPermission", String.class, String.class, int.class)
                    .invoke(platformAccessControl, resourceClass, resourceName,
                            accessLevel);
            return platformClassFactory.convertPlatformReturned(safReturned);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException | ClassNotFoundException | NoSuchFieldException e) {
            throw new SafPlatformError(e.getMessage(), e);
        }
    }
}
