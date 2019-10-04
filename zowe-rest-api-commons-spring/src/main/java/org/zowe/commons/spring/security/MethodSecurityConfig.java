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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.zowe.commons.zos.security.service.PlatformSecurityService;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableConfigurationProperties(SafSecurityConfigurationProperties.class)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
    @Autowired
    private PlatformSecurityService platformSecurityService;

    @Autowired
    private SafSecurityConfigurationProperties safSecurityConfigurationProperties;

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        SafMethodSecurityExpressionHandler expressionHandler = new SafMethodSecurityExpressionHandler(
                platformSecurityService, safSecurityConfigurationProperties);
        return expressionHandler;
    }
}
