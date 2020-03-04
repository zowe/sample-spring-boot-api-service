/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.zos.security.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.zowe.commons.zos.security.platform.MockPlatformAccessControl;
import org.zowe.commons.zos.security.platform.MockPlatformUser;
import org.zowe.commons.zos.security.platform.PlatformAccessControl.AccessLevel;

public class DummyPlatformSecurityServiceTest {
    @Test
    public void testCreateThreadSecurityContext() {
        DummyPlatformSecurityService securityService = new DummyPlatformSecurityService();
        securityService.createThreadSecurityContext("USER", "PASSWORD", null);
        assertEquals("USER", securityService.getCurrentThreadUserId());
    }

    @Test
    public void testCreateDaemonThreadSecurityContext() {
        DummyPlatformSecurityService securityService = new DummyPlatformSecurityService();
        securityService.createThreadSecurityContextByDaemon("USER", null);
        assertEquals("USER", securityService.getCurrentThreadUserId());
    }

    @Test(expected = SecurityRequestFailed.class)
    public void testCreateThreadSecurityContextWithInvalidPassword() {
        DummyPlatformSecurityService securityService = new DummyPlatformSecurityService();
        securityService.createThreadSecurityContext("USER", DummyPlatformSecurityService.INVALID_VALUE, null);
    }

    @Test(expected = SecurityRequestFailed.class)
    public void testCreateThreadSecurityContextWithInvalidUserid() {
        DummyPlatformSecurityService securityService = new DummyPlatformSecurityService();
        securityService.createThreadSecurityContext(DummyPlatformSecurityService.INVALID_VALUE, null, null);
    }

    @Test(expected = SecurityRequestFailed.class)
    public void testCreateThreadSecurityContextByDaemonWithInvalidUserid() {
        DummyPlatformSecurityService securityService = new DummyPlatformSecurityService();
        securityService.createThreadSecurityContextByDaemon(DummyPlatformSecurityService.INVALID_VALUE, null);
    }

    @Test
    public void testRemoveThreadSecurityContext() {
        DummyPlatformSecurityService securityService = new DummyPlatformSecurityService();
        String original = securityService.getCurrentThreadUserId();
        securityService.createThreadSecurityContext("USER", "PASSWORD", null);
        securityService.removeThreadSecurityContext();
        assertEquals(original, securityService.getCurrentThreadUserId());
    }

    @Test
    public void testPermittedResourceAccessCheck() throws Exception {
        DummyPlatformSecurityService securityService = new DummyPlatformSecurityService();
        securityService.afterPropertiesSet();
        assertTrue(securityService.checkPermission("ZOWE", "SAMPLE.RESOURCE", AccessLevel.READ));
        assertTrue(securityService.checkPermission(MockPlatformAccessControl.VALID_USERID, "ZOWE", "SAMPLE.RESOURCE", AccessLevel.READ));
    }

    @Test
    public void testDeniedResourceAccessCheck() throws Exception {
        DummyPlatformSecurityService securityService = new DummyPlatformSecurityService();
        securityService.afterPropertiesSet();
        assertFalse(securityService.checkPermission("ZOWE", "DENIED", AccessLevel.READ));
        assertFalse(securityService.checkPermission(MockPlatformAccessControl.VALID_USERID, "ZOWE", "DENIED", AccessLevel.READ));
    }

    @Test(expected = AccessControlError.class)
    public void testInvalidUseridResourceAccessCheck() throws Exception {
        DummyPlatformSecurityService securityService = new DummyPlatformSecurityService();
        securityService.afterPropertiesSet();
        securityService.checkPermission(MockPlatformUser.INVALID_USERID, "ZOWE", "DENIED", AccessLevel.READ);
    }

    @Test(expected = AccessControlError.class)
    public void testFailingResourceAccessCheck() throws Exception {
        DummyPlatformSecurityService securityService = new DummyPlatformSecurityService();
        securityService.afterPropertiesSet();
        securityService.checkPermission("ZOWE", "FAILING", AccessLevel.READ);
    }

    @Test
    public void testMissingResourceResourceAccessCheck() throws Exception {
        DummyPlatformSecurityService securityService = new DummyPlatformSecurityService();
        securityService.afterPropertiesSet();
        assertFalse(securityService.checkPermission("ZOWE", "UNDEFINED", AccessLevel.READ));
        assertTrue(securityService.checkPermission("ZOWE", "UNDEFINED", AccessLevel.READ, false));
        assertFalse(securityService.checkPermission(MockPlatformAccessControl.VALID_USERID, "ZOWE", "UNDEFINED", AccessLevel.READ));
        assertTrue(securityService.checkPermission(MockPlatformAccessControl.VALID_USERID, "ZOWE", "UNDEFINED", AccessLevel.READ, false));
    }

}
