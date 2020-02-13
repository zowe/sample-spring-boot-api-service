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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;

public class CookieAuthorizationFilter extends AbstractTokenHandler {
    private final ZoweAuthenticationUtility authConfigurationProperties;

    public CookieAuthorizationFilter(ZoweAuthenticationFailureHandler failureHandler,
                                     ZoweAuthenticationUtility authConfigurationProperties) {
        super(failureHandler, authConfigurationProperties);
        this.authConfigurationProperties = authConfigurationProperties;
    }

    /**
     * Extract the valid JWT token from the cookies
     *
     * @param request the http request
     * @return
     */
    @Override
    public Optional<AbstractAuthenticationToken> extractContent(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
            .filter(cookie -> cookie.getName().equals(
                authConfigurationProperties.getCookieProperties().getCookieName()))
            .filter(cookie -> !cookie.getValue().isEmpty())
            .findFirst()
            .map(cookie -> new TokenAuthentication(cookie.getValue()));
    }
}
