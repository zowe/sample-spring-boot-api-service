
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.zowe.commons.zos.security.platform.MockPlatformAccessControl.VALID_USERID;

import org.junit.Test;
import org.zowe.commons.zos.security.platform.PlatformAccessControl.AccessLevel;

public class SafPlatformAccessControlTests {
    private static SafPlatformAccessControl safPlatformAccessControl = new SafPlatformAccessControl(
            new MockPlatformClassFactory());

    @Test
    public void returnsTrueForPermittedCheck() {
        assertNull(safPlatformAccessControl.checkPermission("ZOWE", "SAMPLE.RESOURCE", AccessLevel.READ.getValue()));
    }

    @Test
    public void returnsFalseForDeniedPermittedCheck() {
        assertNotNull(
                safPlatformAccessControl.checkPermission(VALID_USERID, "ZOWE", "DENIED", AccessLevel.READ.getValue()));
        assertNotNull(safPlatformAccessControl.checkPermission(VALID_USERID, "ZOWE", "SAMPLE.RESOURCE",
                AccessLevel.CONTROL.getValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionOnInvalidLevel() {
        new MockPlatformAccessControl("test-saf-invalid.yml");
    }

    @Test(expected = SafPlatformError.class)
    public void returnSafPlatformErrorForInvalidClassNames() {
        SafPlatformAccessControl badPlatformAccessControl = new SafPlatformAccessControl(
                new BadMockPlatformClassFactory());
        assertNull(badPlatformAccessControl.checkPermission("ZOWE", "SAMPLE.RESOURCE", AccessLevel.READ.getValue()));
    }
}
