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

/**
 * Creates classes for platform-specific behavior. The purpose is wrap original
 * classes without interfaces and with static methods into classes that
 * implement interfaces. Implementations of this class factory can either return
 * the original z/OS classes or dummy implementations to enable unit testing or
 * running off z/OS for development purposes.
 */
public interface PlatformClassFactory {
    Class<?> getPlatformReturnedClass() throws ClassNotFoundException;

    Class<?> getPlatformUserClass() throws ClassNotFoundException;

    Object getPlatformUser();

    Class<?> getPlatformAccessControlClass() throws ClassNotFoundException;

    Object getPlatformAccessControl() throws ClassNotFoundException;

    PlatformReturned convertPlatformReturned(Object safReturned)
            throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException;
}
