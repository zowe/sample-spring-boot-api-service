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

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.concurrent.DelegatingSecurityContextCallable;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PlatformThreadLevelSecurityByDaemon implements PlatformThreadLevelSecurity {
    private PlatformSecurityService platformSecurityService;

    @Autowired
    public PlatformThreadLevelSecurityByDaemon(PlatformSecurityService platformSecurityService) {
        this.platformSecurityService = platformSecurityService;
    }

    @Override
    public Runnable wrapRunnableInEnvironmentForAuthenticatedUser(Runnable runnable) {
        return wrapRunnableInEnvironment(runnable, SecurityContextHolder.getContext());
    }

    @Override
    public Runnable wrapRunnableInEnvironment(Runnable runnable, SecurityContext securityContext) {
        return new DelegatingSecurityContextRunnable(new RunInThreadLevelSecurityEnvironmentByDaemon(
                platformSecurityService, runnable, securityContext.getAuthentication()), securityContext);
    }

    @Override
    public Callable wrapCallableInEnvironmentForAuthenticatedUser(Callable callable) {
        return wrapCallableInEnvironment(callable, SecurityContextHolder.getContext());
    }

    @Override
    public Callable wrapCallableInEnvironment(Callable callable, SecurityContext securityContext) {
        return new DelegatingSecurityContextCallable(new CallInThreadLevelSecurityEnvironmentByDaemon(
            platformSecurityService, callable, securityContext.getAuthentication()), securityContext);
    }
}
