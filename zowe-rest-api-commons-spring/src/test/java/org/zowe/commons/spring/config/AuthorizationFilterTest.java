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

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.zowe.commons.spring.token.LoginRequest;
import org.zowe.commons.spring.token.TokenService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AuthorizationFilterTest {

    private static final String SECRET_KEY = "8Zz5tw0Ionm3XPZZfN0NOml3z9FMfmpgXwovR9fp6ryDIoGRM8EPHAB6iHsc0fb";

    @InjectMocks
    private static AuthorizationFilter authorizationFilter;

    @Mock
    private static ZoweAuthenticationUtility authConfigurationProperties;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private FilterChain filterChain;

    Cookie[] cookies = new Cookie[1];

    @Mock
    private TokenService tokenService;

    Optional<AbstractAuthenticationToken> abstractAuthenticationToken;

    private Cookie[] createCookie() {

        Cookie[] cookies = new Cookie[1];

        Cookie tokenCookie = new Cookie("zoweSdkAuthenticationToken", createJwtToken());
        tokenCookie.setComment("Zowe SDK security token");
        tokenCookie.setPath("/");
        tokenCookie.setHttpOnly(true);
        tokenCookie.setMaxAge(-1);
        tokenCookie.setSecure(true);

        cookies[0] = tokenCookie;

        return cookies;
    }

    private String createJwtToken() {
        long expiredTimeMillis = System.currentTimeMillis() + 100000;

        return Jwts.builder()
            .setSubject("zowe")
            .setExpiration(new Date(expiredTimeMillis))
            .signWith(SignatureAlgorithm.HS512, AuthorizationFilterTest.SECRET_KEY)
            .compact();
    }

    @Test
    public void checkIfCookieIsProperlyRead() {
        when(httpServletRequest.getCookies()).thenReturn(createCookie());
        assertNotNull(authorizationFilter.extractContent(httpServletRequest));
    }

    @Test
    public void checkWhenCookieNull() {
        cookies[0] = new Cookie("cookieName", "");
        when(httpServletRequest.getCookies()).thenReturn(cookies);
        assertNotNull(authorizationFilter.extractContent(httpServletRequest));
    }

    @Test
    public void checkWhenCookieIsNotProvided() {
        when(httpServletRequest.getCookies()).thenReturn(null);
        assertNotNull(authorizationFilter.extractContent(httpServletRequest));
    }

    @Before
    public void setup() {
        when(authConfigurationProperties.getJwtSecret()).thenReturn(SECRET_KEY);
    }

    @Test
    public void testPathsNotToBeFiltered() {
        when(httpServletRequest.getRequestURI()).thenReturn("/api/v1/auth/login");
        when(authConfigurationProperties.getServiceLoginEndpoint()).thenReturn("/api/v1/auth/login");
        assertTrue(authorizationFilter.shouldNotFilter(httpServletRequest));
    }

    @Test
    public void testDoFilterInternalForLoginEndpoint() throws ServletException, IOException {
        when(httpServletRequest.getServletPath()).thenReturn("");
        when(httpServletRequest.getRequestURI()).thenReturn("/api/v1/auth/login");
        when(authConfigurationProperties.getServiceLoginEndpoint()).thenReturn("/api/v1/auth/login");
        authorizationFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);
        assertNotEquals(authorizationFilter.extractContent(httpServletRequest), abstractAuthenticationToken);
    }

    @Test
    public void testDoFilterInternalForOtherProtectedEndpoints() throws ServletException, IOException {
        when(httpServletRequest.getServletPath()).thenReturn("");
        when(httpServletRequest.getRequestURI()).thenReturn("/api/v1/auth/query");
        when(authConfigurationProperties.getServiceLoginEndpoint()).thenReturn("/api/v1/auth/login");
        when(authorizationFilter.extractContent(httpServletRequest)).thenReturn(abstractAuthenticationToken);
        Optional<UsernamePasswordAuthenticationToken> usernamePasswordAuthenticationToken = Optional.of(new UsernamePasswordAuthenticationToken("zowe", null, new ArrayList<>()));
        when(httpServletRequest.getHeader(authConfigurationProperties.getAuthorizationHeader())).thenReturn("Bearer " + createJwtToken());
        authorizationFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);
        assertTrue(authorizationFilter.extractContent(httpServletRequest).isPresent());
    }

    @Test(expected = NullPointerException.class)
    public void testDoFilterInternalForOtherProtectedEndpointsException() throws ServletException, IOException {
        when(httpServletRequest.getServletPath()).thenReturn("");
        when(httpServletRequest.getRequestURI()).thenReturn("/api/v1/auth/query");
        when(authConfigurationProperties.getServiceLoginEndpoint()).thenReturn("/api/v1/auth/login");
        when(authorizationFilter.extractContent(httpServletRequest)).thenReturn(abstractAuthenticationToken);
        authorizationFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);
    }

    @Test
    public void testGetAuthenticationWithCookies() throws ServletException, IOException {
        when(httpServletRequest.getCookies()).thenReturn(createCookie());
        assertNotNull(authorizationFilter.getAuthentication(httpServletRequest, httpServletResponse));
    }

    @Test
    public void testGetAuthenticationWithHeaders() throws ServletException, IOException {
        when(authConfigurationProperties.getAuthorizationHeader()).thenReturn(createJwtToken());
        assertNotNull(authorizationFilter.getAuthentication(httpServletRequest, httpServletResponse));
    }

    @Test
    public void testGetAuthenticationWithAuthorizationHeader() throws ServletException, IOException {
        when(httpServletRequest.getHeader(authConfigurationProperties.getAuthorizationHeader())).thenReturn("Bearer " + createJwtToken());
        assertNotNull(authorizationFilter.getAuthentication(httpServletRequest, httpServletResponse));

    }

    @Test
    public void testGetAuthenticationWithCookieAuthentication() throws ServletException, IOException {
        when(httpServletRequest.getHeader(authConfigurationProperties.getAuthorizationHeader())).thenReturn(createJwtToken());
        assertNotNull(authorizationFilter.getAuthentication(httpServletRequest, httpServletResponse));

    }

    @Test
    public void testGetAuthenticationWithBasicAuthentication() throws ServletException, IOException {
        when(httpServletRequest.getHeader(authConfigurationProperties.getAuthorizationHeader())).thenReturn("Basic " + createJwtToken());
        LoginRequest loginRequest = new LoginRequest("zowe", "zowe");
        when(authConfigurationProperties.getCredentialFromAuthorizationHeader(httpServletRequest)).thenReturn(Optional.of(loginRequest));
        when(tokenService.login(loginRequest, httpServletRequest, httpServletResponse)).thenReturn("");
        assertNotNull(authorizationFilter.getAuthentication(httpServletRequest, httpServletResponse));

    }
}
