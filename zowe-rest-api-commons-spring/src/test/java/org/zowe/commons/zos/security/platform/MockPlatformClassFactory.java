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

public class MockPlatformClassFactory implements PlatformClassFactory {

    @Override
    public Class<?> getPlatformUserClass() throws ClassNotFoundException {
        return MockPlatformUser.class;
    }

    @Override
    public Class<?> getPlatformReturnedClass() throws ClassNotFoundException {
        return PlatformReturned.class;
    }

    @Override
    public Object getPlatformUser() {
        return new MockPlatformUser();
    }

    @Override
    public Class<?> getPlatformAccessControlClass() throws ClassNotFoundException {
        return MockPlatformAccessControl.class;
    }

    @Override
    public Object getPlatformAccessControl() throws ClassNotFoundException {
        return new MockPlatformAccessControl();
    }

    @Override
    public PlatformReturned convertPlatformReturned(Object safReturned) throws ClassNotFoundException,
            IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        return (PlatformReturned) safReturned;
    }
}
