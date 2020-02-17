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

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.zowe.commons.spring.token.AbstractTokenHandler;
import org.zowe.commons.spring.token.TokenAuthentication;
import org.zowe.commons.spring.token.TokenService;

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
                               TokenService tokenService) {
        super(failureHandler, authConfigurationProperties, tokenService);
        this.authConfigurationProperties = authConfigurationProperties;
    }

    @Override
    protected Optional<AbstractAuthenticationToken> extractContent(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
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

