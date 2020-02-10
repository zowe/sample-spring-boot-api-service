/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.spring.login;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.zowe.commons.spring.config.ZoweAuthenticationUtility;
import org.zowe.commons.spring.token.TokenService;

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
    private final ZoweAuthenticationUtility authConfigurationProperties;
    private final TokenService tokenService;

    public LoginFilter(String authEndpoint,
                       ZoweAuthenticationUtility authConfigurationProperties,
                       AuthenticationManager authenticationManager, TokenService tokenService) {
        super(authEndpoint);
        this.authConfigurationProperties = authConfigurationProperties;
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

        if (StringUtils.isBlank(loginRequest.getUsername()) || StringUtils.isBlank(loginRequest.getPassword())) {
            throw new AuthenticationCredentialsNotFoundException("Username or password not provided.");
        }
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
        String token = authConfigurationProperties.createToken(authResult);

        authConfigurationProperties.setCookie(token, response);
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }
}
