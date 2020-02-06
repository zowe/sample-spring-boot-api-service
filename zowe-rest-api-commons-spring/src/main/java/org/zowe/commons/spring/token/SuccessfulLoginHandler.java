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

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class SuccessfulLoginHandler implements AuthenticationSuccessHandler {

    private final AuthConfigurationProperties authConfigurationProperties;


    /**
     * Set cookie and http response on successful authentication
     *
     * @param request        the http request
     * @param response       the http response
     * @param authentication the successful authentication
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String token = createToken(authentication);

        setCookie(token, response);
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }

    public String createToken(Authentication authentication) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
            .setSubject(authentication.getName())
            .setExpiration(new Date(now + authConfigurationProperties.getTokenProperties().getExpirationTime() * 1000))
            .signWith(SignatureAlgorithm.HS512, authConfigurationProperties.getTokenProperties().getSecretKeyToGenJWTs())
            .setIssuedAt(new Date(now))
            .compact();
    }


    /**
     * Method to set the cookie in the response. Which will contain the JWT token,HTTP flag etc.
     *
     * @param token
     * @param response
     */
    public void setCookie(String token, HttpServletResponse response) {
        Cookie tokenCookie = new Cookie(authConfigurationProperties.getCookieProperties().getCookieName(),
            token);
        tokenCookie.setComment(authConfigurationProperties.getCookieProperties().getCookieComment());
        tokenCookie.setPath(authConfigurationProperties.getCookieProperties().getCookiePath());
        tokenCookie.setHttpOnly(true);
        tokenCookie.setMaxAge(authConfigurationProperties.getCookieProperties().getCookieMaxAge());
        tokenCookie.setSecure(authConfigurationProperties.getCookieProperties().isCookieSecure());

        response.addCookie(tokenCookie);
    }
}
