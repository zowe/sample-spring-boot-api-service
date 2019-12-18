
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.VALID_PASSWORD;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.VALID_USERID;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.INVALID_PASSWORD;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.INVALID_USERID;

import org.junit.Test;

public class SafPlatformUserTests {
    private static SafPlatformUser safPlatformUser = new SafPlatformUser(new MockPlatformClassFactory());

    @Test
    public void returnsNullForValidAuthentication() {
        assertNull(safPlatformUser.authenticate(VALID_USERID, VALID_PASSWORD));
    }

    @Test
    public void returnsErrorDetailsForInvalidAuthentication() {
        PlatformReturned returned = safPlatformUser.authenticate(INVALID_USERID, INVALID_PASSWORD);
        assertFalse(returned.isSuccess());
    }

    @Test
    public void returnsErrorDetailsForEmptyPassword() {
        PlatformReturned returned = safPlatformUser.authenticate(VALID_USERID, "");
        assertFalse(returned.isSuccess());
        assertEquals(PlatformPwdErrno.EINVAL.errno, returned.getErrno());
        assertEquals(0, returned.getRc());
        assertEquals(0x090C02A7, returned.getErrno2());
        assertEquals(PlatformErrno2.JRPasswordLenError, PlatformErrno2.valueOfErrno(returned.getErrno2()));
    }

    @Test
    public void safPlatformErrorCanBeInstantiated() {
        assertNotNull(new SafPlatformError("test"));
        assertNotNull(new SafPlatformError("test", new Exception()));
        assertNotNull(new SafPlatformError(new Exception()));
    }


    @Test(expected = SafPlatformError.class)
    public void returnSafPlatformErrorForInvalidClassNames() {
        SafPlatformUser badPlatformUser = new SafPlatformUser(new BadMockPlatformClassFactory());
        assertNull(badPlatformUser.authenticate(VALID_USERID, VALID_PASSWORD));
    }
}
