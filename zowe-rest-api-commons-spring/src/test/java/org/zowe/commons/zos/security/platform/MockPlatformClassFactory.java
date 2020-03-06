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
