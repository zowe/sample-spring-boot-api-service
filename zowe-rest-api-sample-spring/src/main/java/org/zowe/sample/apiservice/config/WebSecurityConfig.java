/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.zowe.commons.security.AuthConfigurationProperties;
import org.zowe.commons.security.SuccessfulLoginHandler;
import org.zowe.commons.security.ZoweWebSecurityConfig;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationProvider;

@Configuration
@EnableWebSecurity
@ComponentScan("org.zowe.commons.zos.security")
public class WebSecurityConfig extends ZoweWebSecurityConfig {
    @Autowired
    AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private ZosAuthenticationProvider authProvider;

    public WebSecurityConfig(AuthConfigurationProperties authConfigurationProperties, SuccessfulLoginHandler successfulLoginHandler, ObjectMapper securityObjectMapper) {
        super(authConfigurationProperties, successfulLoginHandler, securityObjectMapper);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider);
    }
}
