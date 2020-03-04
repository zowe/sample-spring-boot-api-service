/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.spring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.zowe.commons.spring.token.TokenService;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationProvider;

@Configuration
public class ZoweWebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final ZoweAuthenticationUtility authConfigurationProperties;
    private final ZoweAuthenticationFailureHandler tokenFailureHandler;

    @Autowired
    ZosAuthenticationProvider authenticationProvider;

    @Autowired
    TokenService tokenService;

    public ZoweWebSecurityConfig(ZoweAuthenticationUtility authConfigurationProperties,
                                 ZoweAuthenticationFailureHandler tokenFailureHanlder) {
        this.authConfigurationProperties = authConfigurationProperties;
        this.tokenFailureHandler = tokenFailureHanlder;
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

            .and()
            .authorizeRequests()
            .antMatchers(authConfigurationProperties.getServiceLoginEndpoint(), "/swagger-ui.html", "/",
                "/apiDocs/**", "/webjars/**", "/swagger-resources/**", "/favicon/**", "/api/v1/apiDocs/**", "/csrf",
                "/api/*/apiDocs", "/actuator/info", "/actuator/health").permitAll()
            .anyRequest().authenticated()

            .and()
            .addFilterBefore(new AuthorizationFilter(tokenFailureHandler, authConfigurationProperties, tokenService), UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * Method to provide Authentication provider as it cant be null
     * in spring security filter chain
     *
     * @param auth
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider);
    }
}
