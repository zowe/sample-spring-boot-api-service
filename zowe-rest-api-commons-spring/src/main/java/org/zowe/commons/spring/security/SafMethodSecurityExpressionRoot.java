/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.spring.security;

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.zowe.commons.zos.security.platform.PlatformAccessControl.AccessLevel;
import org.zowe.commons.zos.security.service.PlatformSecurityService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SafMethodSecurityExpressionRoot extends SecurityExpressionRoot
        implements MethodSecurityExpressionOperations {
    private Object filterObject;
    private Object returnObject;

    private final PlatformSecurityService platformSecurityService;
    private final SafSecurityConfigurationProperties safSecurityConfigurationProperties;

    public SafMethodSecurityExpressionRoot(Authentication authentication, PlatformSecurityService platformSecurityService, SafSecurityConfigurationProperties safSecurityConfigurationProperties) {
        super(authentication);
        this.platformSecurityService = platformSecurityService;
        this.safSecurityConfigurationProperties = safSecurityConfigurationProperties;
    }

    public boolean hasSafResourceAccess(String resourceClass, String resourceName, String accessLevel) {
        String userid = authentication.getName();
        AccessLevel level = AccessLevel.valueOf(accessLevel);
        log.debug("Evaluating access of user {} to resource {} in class {} level {}", userid, resourceName, level);
        return platformSecurityService.checkPermission(userid, resourceClass, resourceName, level);
    }

    public boolean hasSafServiceResourceAccess(String resourceNameSuffix, String accessLevel) {
        return hasSafResourceAccess(safSecurityConfigurationProperties.getServiceResourceClass(), safSecurityConfigurationProperties.getServiceResourceNamePrefix() + resourceNameSuffix, accessLevel);
    }

    @Override
    public Object getFilterObject() {
        return this.filterObject;
    }

    @Override
    public Object getReturnObject() {
        return this.returnObject;
    }

    @Override
    public Object getThis() {
        return this;
    }

    @Override
    public void setFilterObject(Object obj) {
        this.filterObject = obj;
    }

    @Override
    public void setReturnObject(Object obj) {
        this.returnObject = obj;
    }
}
