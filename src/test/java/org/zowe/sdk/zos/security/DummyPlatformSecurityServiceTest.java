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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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

    @Test
    public void testRemoveThreadSecurityContext() {
        DummyPlatformSecurityService securityService = new DummyPlatformSecurityService();
        String original = securityService.getCurrentThreadUserId();
        securityService.createThreadSecurityContext("USER", "PASSWORD", null);
        securityService.removeThreadSecurityContext();
        assertEquals(original, securityService.getCurrentThreadUserId());
    }
}
