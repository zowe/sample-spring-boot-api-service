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
 * Interface to do z/OS user authentication.
 *
 * It is inspired by
 * https://www.ibm.com/support/knowledgecenter/SSYKE2_8.0.0/com.ibm.java.zsecurity.api.80.doc/com.ibm.os390.security/com/ibm/os390/security/PlatformUser.html
 * class. But it provides interface instead of class with static methods so
 * non-z/OS implementation of this interface can be created.
 */
public interface PlatformUser {
    /**
     * Authenticates an user.
     *
     * @return If successful, a null object is returned. If not successful an instance of
     * the PlatformReturned class is returned.
     */
    PlatformReturned authenticate(String userid, String password);
}
