/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.spring.security;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.zowe.commons.zos.security.service.PlatformSecurityService;

public class SafMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {
    private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();
    private final PlatformSecurityService platformSecurityService;
    private final SafSecurityConfigurationProperties safSecurityConfigurationProperties;

    public SafMethodSecurityExpressionHandler(PlatformSecurityService platformSecurityService,
            SafSecurityConfigurationProperties safSecurityConfigurationProperties) {
        this.platformSecurityService = platformSecurityService;
        this.safSecurityConfigurationProperties = safSecurityConfigurationProperties;
    }

    @Override
    protected MethodSecurityExpressionOperations createSecurityExpressionRoot(Authentication authentication,
            MethodInvocation invocation) {
        SafMethodSecurityExpressionRoot root = new SafMethodSecurityExpressionRoot(authentication,
                platformSecurityService, safSecurityConfigurationProperties);
        root.setPermissionEvaluator(getPermissionEvaluator());
        root.setTrustResolver(this.trustResolver);
        root.setRoleHierarchy(getRoleHierarchy());
        return root;
    }
}
