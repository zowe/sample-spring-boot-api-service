/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sdk.zos.security.thread;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.zowe.sdk.zos.security.service.DummyPlatformSecurityService;
import org.zowe.sdk.zos.security.service.PlatformSecurityService;

public class PlatformThreadLevelSecurityByDaemonTest {
    @Test
    public void testWrapRunnableInEnvironmentForAuthenticatedUser() {
        PlatformSecurityService platformSecurityService = new DummyPlatformSecurityService();
        PlatformThreadLevelSecurityByDaemon platformThreadLevelSecurity = new PlatformThreadLevelSecurityByDaemon(
                platformSecurityService);

        Map<String, String> values = new HashMap<>();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("TEST");
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));

        String originalThreadUserId = platformSecurityService.getCurrentThreadUserId();

        platformThreadLevelSecurity.wrapRunnableInEnvironmentForAuthenticatedUser(new Runnable() {
            @Override
            public void run() {
                values.put("runnableUserId", platformSecurityService.getCurrentThreadUserId());
                values.put("runnableAuthenticationName",
                        SecurityContextHolder.getContext().getAuthentication().getName());
            }
        }).run();

        assertEquals(originalThreadUserId, platformSecurityService.getCurrentThreadUserId());
        assertEquals("TEST", values.get("runnableUserId"));
        assertEquals("TEST", values.get("runnableAuthenticationName"));
    }

    @Test
    public void testWrapCallableInEnvironmentForAuthenticatedUser() throws Exception {
        PlatformSecurityService platformSecurityService = new DummyPlatformSecurityService();
        PlatformThreadLevelSecurityByDaemon platformThreadLevelSecurity = new PlatformThreadLevelSecurityByDaemon(
                platformSecurityService);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("TEST");
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));

        String originalThreadUserId = platformSecurityService.getCurrentThreadUserId();

        String callableUserId = (String) platformThreadLevelSecurity
                .wrapCallableInEnvironmentForAuthenticatedUser(new Callable<String>() {
                    @Override
                    public String call() {
                        return platformSecurityService.getCurrentThreadUserId();
                    }
                }).call();

        assertEquals(originalThreadUserId, platformSecurityService.getCurrentThreadUserId());
        assertEquals("TEST", callableUserId);
    }
}
