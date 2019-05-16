
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.zowe.sdk.zos.security.MockPlatformUser.VALID_PASSWORD;
import static org.zowe.sdk.zos.security.MockPlatformUser.VALID_USERID;
import static org.zowe.sdk.zos.security.MockPlatformUser.INVALID_PASSWORD;
import static org.zowe.sdk.zos.security.MockPlatformUser.INVALID_USERID;

import org.junit.Test;

public class SafPlatformUserTests {
    private static SafPlatformUser safPlatformUser = new SafPlatformUser(new MockPlatformClassFactory());

    @Test
    public void returnsNullForValidAuthentication() {
        assertNull(safPlatformUser.authenticate(VALID_USERID, VALID_PASSWORD));
    }

    @Test
    public void returnsDetailsForInvalidAuthentication() {
        PlatformReturned returned = safPlatformUser.authenticate(INVALID_USERID, INVALID_PASSWORD);
        assertFalse(returned.isSuccess());
    }

}
