/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */

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
    public void returnSafPlatformErrorForInvalidClassNamesWhenCallingCheckPermision() {
        SafPlatformAccessControl badPlatformAccessControl = new SafPlatformAccessControl(
                new BadMockPlatformClassFactory());
        assertNull(badPlatformAccessControl.checkPermission("ZOWE", "SAMPLE.RESOURCE", AccessLevel.READ.getValue()));
    }

    @Test(expected = SafPlatformError.class)
    public void returnSafPlatformErrorForInvalidClassNamesWhenCallingCheckPermisionWithUserid() {
        SafPlatformAccessControl badPlatformAccessControl = new SafPlatformAccessControl(
                new BadMockPlatformClassFactory());
        assertNull(badPlatformAccessControl.checkPermission("ZOWE", "ZOWE", "SAMPLE.RESOURCE", AccessLevel.READ.getValue()));
    }
}
