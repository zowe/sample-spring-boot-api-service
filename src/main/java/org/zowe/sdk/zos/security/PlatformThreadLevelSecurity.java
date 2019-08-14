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

import org.springframework.security.core.context.SecurityContext;

public interface PlatformThreadLevelSecurity {
    Runnable wrapRunnableInEnvironmentForAuthenticatedUser(Runnable runnable);

    Runnable wrapRunnableInEnvironment(Runnable runnable, SecurityContext authentication);

    Callable wrapCallableInEnvironmentForAuthenticatedUser(Callable runnable);

    Callable wrapCallableInEnvironment(Callable runnable, SecurityContext authentication);
}
