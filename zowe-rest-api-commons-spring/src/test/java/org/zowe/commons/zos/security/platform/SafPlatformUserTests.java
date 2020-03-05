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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.INVALID_PASSWORD;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.INVALID_USERID;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.VALID_PASSWORD;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.VALID_USERID;

import org.junit.Test;

public class SafPlatformUserTests {
    private static SafPlatformUser safPlatformUser = new SafPlatformUser(new MockPlatformClassFactory());

    @Test
    public void returnsNullForValidAuthentication() {
        assertNull(safPlatformUser.authenticate(VALID_USERID, VALID_PASSWORD));
    }

    @Test
    public void worksWithSetApplid() {
        SafUtils.setThreadApplid("APPLID");
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
