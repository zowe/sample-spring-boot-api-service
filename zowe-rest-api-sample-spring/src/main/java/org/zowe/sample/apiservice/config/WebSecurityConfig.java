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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationProvider;

@Configuration
@EnableWebSecurity
@ComponentScan("org.zowe.commons.zos.security")
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${apiml.service.title:serice}")
    private String serviceTitle;

    @Autowired
    AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
        http.headers().httpStrictTransportSecurity().disable();
        http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint);
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.httpBasic().realmName(serviceTitle);
        http.authorizeRequests().antMatchers("/", "/swagger-ui.html", "/webjars/springfox-swagger-ui/**", "/apiDocs/**",
                "/api/*/apiDocs", "/swagger-resources/**", "/csrf").permitAll().anyRequest().authenticated();
    }

    @Autowired
    private ZosAuthenticationProvider authProvider;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider);
    }
}
