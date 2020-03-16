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
import io.jsonwebtoken.security.Keys;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.zowe.commons.spring.token.LoginRequest;
import org.zowe.commons.spring.token.QueryResponse;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationProvider;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AuthorizationFilterTest {

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

    Key key;

    Cookie[] cookies = new Cookie[1];

    @Mock
    private static ZosAuthenticationProvider zosAuthenticationProvider;

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
        key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        return Jwts.builder()
            .setSubject("zowe")
            .signWith(key)
            .setIssuedAt(new Date(System.currentTimeMillis() + 100000))
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
        when(authConfigurationProperties.getClaims(any())).thenReturn(new QueryResponse("zowe", new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis())));
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
        assertNotNull(authorizationFilter.getAuthentication(httpServletRequest));
    }

    @Test
    public void testGetAuthenticationWithHeaders() throws ServletException, IOException {
        when(authConfigurationProperties.getAuthorizationHeader()).thenReturn(createJwtToken());
        assertNotNull(authorizationFilter.getAuthentication(httpServletRequest));
    }

    @Test
    public void testGetAuthenticationWithAuthorizationHeader() throws ServletException, IOException {
        String token = "Bearer " + createJwtToken();
        when(httpServletRequest.getHeader(authConfigurationProperties.getAuthorizationHeader())).thenReturn(token);
        assertNotNull(authorizationFilter.getAuthentication(httpServletRequest));

    }

    @Test
    public void testGetAuthenticationWithCookieAuthentication() throws ServletException, IOException {
        when(httpServletRequest.getHeader(authConfigurationProperties.getAuthorizationHeader())).thenReturn(createJwtToken());
        assertNotNull(authorizationFilter.getAuthentication(httpServletRequest));

    }

    @Test
    public void testGetAuthenticationWithBasicAuthentication() throws Exception {
        when(httpServletRequest.getHeader(authConfigurationProperties.getAuthorizationHeader())).thenReturn("Basic " + createJwtToken());
        LoginRequest loginRequest = new LoginRequest("zowe", "zowe");
        when(authConfigurationProperties.getCredentialFromAuthorizationHeader(httpServletRequest)).thenReturn(Optional.of(loginRequest));
        when(zosAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken("zowe", "zowe"))).
            thenReturn(new UsernamePasswordAuthenticationToken("zowe", null, new ArrayList<>()));
        assertNotNull(authorizationFilter.getAuthentication(httpServletRequest));
    }

    @Test
    public void checkIfHeaderIsProperlyProvided() {
        when(httpServletRequest.getHeader(authConfigurationProperties.getAuthorizationHeader())).thenReturn(ZoweAuthenticationUtility.BASIC_AUTHENTICATION_PREFIX + " " + createJwtToken());
        assertNotNull(authorizationFilter.extractContent(httpServletRequest));
    }

    @Test
    public void testListOfAllowedEndPoints() throws ServletException, IOException {
        assertTrue(authorizationFilter.listOfAllowedEndpoints("/swagger-ui.html"));
        assertTrue(authorizationFilter.listOfAllowedEndpoints("/webjars/"));
        assertTrue(authorizationFilter.listOfAllowedEndpoints("/login"));
        assertTrue(authorizationFilter.listOfAllowedEndpoints("/swagger-resources"));
        assertTrue(authorizationFilter.listOfAllowedEndpoints("/apiDocs"));
        assertTrue(authorizationFilter.listOfAllowedEndpoints("/favicon"));
        assertTrue(authorizationFilter.listOfAllowedEndpoints("/"));
        assertTrue(authorizationFilter.listOfAllowedEndpoints("/csrf"));
    }

}
