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

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.concurrent.DelegatingSecurityContextCallable;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.zowe.commons.zos.security.service.PlatformSecurityService;

@Service
public class PlatformThreadLevelSecurityByDaemon implements PlatformThreadLevelSecurity {
    private final PlatformSecurityService platformSecurityService;

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
    public <T> Callable<T> wrapCallableInEnvironmentForAuthenticatedUser(Callable<T> callable) {
        return wrapCallableInEnvironment(callable, SecurityContextHolder.getContext());
    }

    @Override
    public <T> Callable<T> wrapCallableInEnvironment(Callable<T> callable, SecurityContext securityContext) {
        return new DelegatingSecurityContextCallable<>(new CallInThreadLevelSecurityEnvironmentByDaemon<>(
                platformSecurityService, callable, securityContext.getAuthentication()), securityContext);
    }

}
