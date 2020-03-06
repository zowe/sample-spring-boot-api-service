/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.zos.security.thread;

import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;
import org.zowe.commons.zos.security.service.PlatformSecurityService;

public final class RunInThreadLevelSecurityEnvironmentByDaemon implements Runnable {
    private final PlatformSecurityService service;
    private final Runnable runnable;
    private final Authentication authentication;

    public RunInThreadLevelSecurityEnvironmentByDaemon(PlatformSecurityService service, Runnable runnable,
            Authentication authentication) {
        Assert.notNull(service, "service cannot be null");
        Assert.notNull(runnable, "runnable cannot be null");
        Assert.notNull(authentication, "authentication cannot be null");
        this.service = service;
        this.runnable = runnable;
        this.authentication = authentication;
    }

    @Override
    public void run() {
        createSecurityEnvironment();
        try {
            runnable.run();
        } finally {
            service.removeThreadSecurityContext();
        }
    }

    private void createSecurityEnvironment() {
        service.createThreadSecurityContextByDaemon(authentication.getName(), null);
    }

    @Override
    public String toString() {
        return runnable.toString();
    }
}
