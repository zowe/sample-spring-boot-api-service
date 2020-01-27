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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.zowe.sample.apiservice.security.SampleApiAuthenticationProvider;
import org.zowe.sample.apiservice.security.JWTAuthorizationFilter;

@Configuration
@EnableWebSecurity
@ComponentScan("org.zowe.zos.security")
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final AuthConfigurationProperties authConfigurationProperties;

    public WebSecurityConfig(AuthConfigurationProperties authConfigurationProperties) {
        this.authConfigurationProperties = authConfigurationProperties;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        //TODO: what could be the best configuration for SDK in terms of CSRF and other security parameters
        http.csrf().disable()
            .headers()
            .httpStrictTransportSecurity().disable()
            .frameOptions().disable()

            //Session config
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            //login endpoint
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, authConfigurationProperties.getServiceLoginEndpoint()).permitAll()
            .anyRequest().authenticated()

            .and()
            .addFilter(new JWTAuthorizationFilter(authenticationManager(), authConfigurationProperties));
    }

    @Autowired
    private SampleApiAuthenticationProvider authProvider;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider);
    }
}
