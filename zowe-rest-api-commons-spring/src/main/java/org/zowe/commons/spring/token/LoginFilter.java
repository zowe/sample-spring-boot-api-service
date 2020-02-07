/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.spring.token;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;


/**
 * Filter to authenticate the user and to set Jwt token in cookie.
 */
public class LoginFilter extends AbstractAuthenticationProcessingFilter {
    private final AuthConfigurationProperties authConfigurationProperties;
    private final SuccessfulLoginHandler successHandler;
    private final TokenService tokenService;

    public LoginFilter(String authEndpoint,
                       AuthConfigurationProperties authConfigurationProperties,
                       AuthenticationManager authenticationManager, SuccessfulLoginHandler successHandler, TokenService tokenService) {
        super(authEndpoint);
        this.authConfigurationProperties = authConfigurationProperties;
        this.successHandler = successHandler;
        this.tokenService = tokenService;
        this.setAuthenticationManager(authenticationManager);
    }

    /**
     * Calls TokenService interface to validate the username and password
     *
     * @param request  the http request
     * @param response the http response
     * @return the authenticated token
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Optional<LoginRequest> optionalLoginRequest = authConfigurationProperties.getCredentialFromAuthorizationHeader(request);
        LoginRequest loginRequest = optionalLoginRequest.orElseGet(() -> authConfigurationProperties.getCredentialsFromBody(request));

        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

        tokenService.login(loginRequest, request);

        return auth;
    }

    /**
     * Calls successful login handler
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        successHandler.onAuthenticationSuccess(request, response, authResult);
    }
}
