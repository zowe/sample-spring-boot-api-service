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
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {  // NOSONAR
    @Autowired
    private PlatformSecurityService platformSecurityService;

    @Autowired
    private SafSecurityConfigurationProperties safSecurityConfigurationProperties;

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        return new SafMethodSecurityExpressionHandler(platformSecurityService, safSecurityConfigurationProperties);
    }
}
