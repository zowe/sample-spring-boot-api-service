/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.spring.config;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.zowe.commons.spring.token.AbstractTokenHandler;
import org.zowe.commons.spring.token.TokenAuthentication;
import org.zowe.commons.spring.token.TokenService;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationProvider;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;

/**
 * Filter to validate JWT token in all the rest API's.
 */
public class AuthorizationFilter extends AbstractTokenHandler {

    private final ZoweAuthenticationUtility authConfigurationProperties;

    public AuthorizationFilter(ZoweAuthenticationFailureHandler failureHandler,
                               ZoweAuthenticationUtility authConfigurationProperties,
                               ZosAuthenticationProvider authenticationManager) {
        super(failureHandler, authConfigurationProperties, authenticationManager);
        this.authConfigurationProperties = authConfigurationProperties;
    }

    @Override
    protected Optional<AbstractAuthenticationToken> extractContent(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (null != request.getHeader(authConfigurationProperties.getAuthorizationHeader())
            && request.getHeader(authConfigurationProperties.getAuthorizationHeader()).startsWith(ZoweAuthenticationUtility.BASIC_AUTHENTICATION_PREFIX)) {
            return Optional.of(new TokenAuthentication(request.getHeader(authConfigurationProperties.getAuthorizationHeader())));
        } else if (null != cookies) {
            Optional<AbstractAuthenticationToken> authToken = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(authConfigurationProperties.getCookieTokenName()))
                .filter(cookie -> !cookie.getValue().isEmpty())
                .findFirst()
                .map(cookie -> new TokenAuthentication(cookie.getValue()));

            //return the token from the authorization header if cookies aren't present
            return authToken.map(Optional::of).orElseGet(() -> Optional.of(
                new TokenAuthentication(request.getHeader(authConfigurationProperties.getAuthorizationHeader()))));
        } else {
            return Optional.of(new TokenAuthentication(request.getHeader(authConfigurationProperties.getAuthorizationHeader())));
        }
    }
}

