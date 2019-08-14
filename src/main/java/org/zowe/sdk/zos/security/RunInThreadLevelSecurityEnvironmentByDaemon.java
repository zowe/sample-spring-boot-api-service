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

import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

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
