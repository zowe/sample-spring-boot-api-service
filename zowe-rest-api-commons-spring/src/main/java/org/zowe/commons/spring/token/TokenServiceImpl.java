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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationProvider;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenServiceImpl extends ZosAuthenticationProvider implements TokenService {
    private final AuthConfigurationProperties authConfigurationProperties;
    private final AuthenticationFailureHandler failureHandler;

    /**
     * Calls authentication manager to validate the username and password
     *
     * @return the authenticated token
     */
    @Override
    public ResponseEntity login(LoginRequest loginRequest,
                                HttpServletRequest request) throws ServletException {
        if (loginRequest == null) {
            loginRequest = authConfigurationProperties.getCredentialFromAuthorizationHeader(request).get();
        }

        UsernamePasswordAuthenticationToken authentication
            = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

        authenticate(authentication);
        return ResponseEntity.ok(new TokenResponse(createToken(authentication)));
    }

    /**
     * This method is used to create token with Jwts library.
     *
     * @param authentication
     * @return
     */
    private String createToken(Authentication authentication) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
            .setSubject(authentication.getName())
            .setExpiration(new Date(now + authConfigurationProperties.getTokenProperties().getExpirationTime() * 1000))
            .signWith(SignatureAlgorithm.HS512, authConfigurationProperties.getTokenProperties().getSecretKeyToGenJWTs())
            .setIssuedAt(new Date(now))
            .compact();
    }
}
