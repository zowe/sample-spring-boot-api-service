/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationProvider;

public class ZoweWebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final AuthConfigurationProperties authConfigurationProperties;
    private SuccessfulLoginHandler successfulLoginHandler;
    private final ObjectMapper securityObjectMapper;

    public ZoweWebSecurityConfig(AuthConfigurationProperties authConfigurationProperties, SuccessfulLoginHandler successfulLoginHandler, ObjectMapper securityObjectMapper) {
        this.authConfigurationProperties = authConfigurationProperties;
        this.successfulLoginHandler = successfulLoginHandler;
        this.securityObjectMapper = securityObjectMapper;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        //CSRF configuration
        http.csrf()
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).ignoringAntMatchers("/api/**")

            .and()
            .headers()
            .httpStrictTransportSecurity().disable()
            .frameOptions().disable()

            //Session config
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            //Login endpoint
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.POST, authConfigurationProperties.getServiceLoginEndpoint()).permitAll()
            .anyRequest().authenticated()

            .and()
            .addFilterBefore(loginFilter(authConfigurationProperties.getServiceLoginEndpoint()), UsernamePasswordAuthenticationFilter.class)
            //Filter to validate JWT token in all the rest endpoints
            .addFilter(new JWTAuthorizationFilter(authenticationManager(), authConfigurationProperties));
    }

    /**
     * Processes /login requests
     */
    private LoginFilter loginFilter(String loginEndpoint) throws Exception {
        return new LoginFilter(
            loginEndpoint,
            securityObjectMapper,
            authenticationManager(),
            successfulLoginHandler,
            authConfigurationProperties);
    }

    @Autowired
    private ZosAuthenticationProvider authProvider;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider);
    }
}
