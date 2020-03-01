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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.zowe.commons.spring.token.LoginRequest;
import org.zowe.commons.spring.token.QueryResponse;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;


/**
 * Configuration class for authentication-related security properties
 */
@Data
@Component
@Slf4j
public class ZoweAuthenticationUtility {

    public static final String BASIC_AUTHENTICATION_PREFIX = "Basic ";
    public static final String BEARER_AUTHENTICATION_PREFIX = "Bearer ";
    private String serviceLoginEndpoint = "/api/v1/auth/login";
    private String authorizationHeader = "Authorization";

    @Value("${zowe.commons.security.token.cookieTokenName:zoweSdkAuthenticationToken}")
    private String cookieTokenName;

    @Value("${zowe.commons.security.token.expiration:86400000}")
    private int expiration;

    @Value("${zowe.commons.security.token.secretKeyToGenJWTs:8Zz5tw0Ionm3XPZZfN0NOml3z9FM}")
    private String secretKey;

    @Autowired
    ZoweAuthenticationFailureHandler zoweAuthenticationFailureHandler;

    /**
     * Decode the encoded credentials
     *
     * @param base64Credentials the credentials encoded in base64
     * @return the decoded credentials in {@link LoginRequest}
     */
    public LoginRequest mapBase64Credentials(String base64Credentials) {
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        int i = credentials.indexOf(':');
        if (i > 0) {
            return new LoginRequest(credentials.substring(0, i), credentials.substring(i + 1));
        } else {
            throw new AuthenticationCredentialsNotFoundException("Password is not provided");
        }
    }

    /**
     * Extract credentials from the authorization header in the request and decode them
     *
     * @param request the http request
     * @return the decoded credentials
     */
    public Optional<LoginRequest> getCredentialFromAuthorizationHeader(HttpServletRequest request) {
        return Optional.ofNullable(
            request.getHeader(HttpHeaders.AUTHORIZATION)
        ).filter(
            header -> header.startsWith(BASIC_AUTHENTICATION_PREFIX)
        ).map(
            header -> header.replaceFirst(BASIC_AUTHENTICATION_PREFIX, "").trim()
        )
            .filter(base64Credentials -> !base64Credentials.isEmpty())
            .map(this::mapBase64Credentials);
    }

    /**
     * This method is used to create token with Jwts library.
     *
     * @param authentication
     * @return
     */
    public String createToken(Authentication authentication) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
            .setSubject(authentication.getName())
            .setExpiration(new Date(now + Integer.valueOf(expiration)))
            .signWith(SignatureAlgorithm.HS512, secretKey)
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
        Cookie tokenCookie = new Cookie(cookieTokenName, token);
        tokenCookie.setComment("Zowe SDK security token");
        tokenCookie.setPath("/");
        tokenCookie.setHttpOnly(true);
        tokenCookie.setMaxAge(-1);
        tokenCookie.setSecure(true);

        response.addCookie(tokenCookie);
    }

    /**
     * Get the Claims from the Token String in the authentication header/cookie
     *
     * @param jwtToken the token either form the authentication header or the cookie
     * @return extracts the claims from the token and returns it
     */
    public QueryResponse getClaims(String jwtToken) {
        jwtToken = jwtToken.replaceFirst(ZoweAuthenticationUtility.BEARER_AUTHENTICATION_PREFIX, "").trim();
        Claims claims = Jwts.parser()
            .setSigningKey(DatatypeConverter.parseBase64Binary(secretKey))
            .parseClaimsJws(jwtToken).getBody();
        return new QueryResponse(claims.getSubject(), claims.getIssuedAt(), claims.getExpiration());
    }
}
