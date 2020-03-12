/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.spring.token;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.zowe.commons.spring.config.ZoweAuthenticationFailureHandler;
import org.zowe.commons.spring.config.ZoweAuthenticationUtility;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationProvider;

import javax.crypto.SecretKey;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TokenServiceImplTest {

    @Mock
    HttpServletResponse httpServletResponse;

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    ZosAuthenticationProvider zosAuthenticationProvider;

    @Mock
    ZoweAuthenticationUtility authConfigurationProperties;

    @Mock
    ZoweAuthenticationFailureHandler zoweAuthenticationFailureHandler;

    @InjectMocks
    TokenServiceImpl tokenService;

    String token = null;
    private LoginRequest loginRequest = new LoginRequest("zowe", "zowe");
    private UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
    SecretKey key;

    @Before
    public void initMocks() {
        key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        token = Jwts.builder()
            .setSubject("zowe")
            .signWith(key)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .compact();

        MockitoAnnotations.initMocks(this);
        when(zosAuthenticationProvider.authenticate(authenticationToken)).thenReturn(authenticationToken);
        ReflectionTestUtils.setField(authConfigurationProperties, "cookieTokenName", "zoweSdkAuthenticationToken");
        ReflectionTestUtils.setField(authConfigurationProperties, "keyStoreType", "PKCS12");
        ReflectionTestUtils.setField(authConfigurationProperties, "keyAlias", "jwtsecret");
    }

    @Test
    public void verifyLogin() throws ServletException {
        when(authConfigurationProperties.createToken(authenticationToken)).thenCallRealMethod().
            thenReturn("token");
        when(authConfigurationProperties.getJwtSecret()).thenReturn(key);
        Assert.assertNotNull(tokenService.login(loginRequest, httpServletRequest, httpServletResponse));
    }

    @Test
    public void verifyLoginWithBasic() throws ServletException {
        ReflectionTestUtils.setField(authConfigurationProperties, "cookieTokenName", "zoweSdkAuthenticationToken");
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic "
            + Base64.getEncoder().encodeToString(("zowe" + ":" + "zowe").getBytes()));
        when(authConfigurationProperties.getCredentialFromAuthorizationHeader(httpServletRequest)).thenCallRealMethod().
            thenReturn(java.util.Optional.ofNullable(loginRequest));
        when(authConfigurationProperties.mapBase64Credentials(any())).thenCallRealMethod().
            thenReturn(loginRequest);
        when(authConfigurationProperties.createToken(authenticationToken)).thenCallRealMethod().
            thenReturn("token");
        Mockito.doCallRealMethod().when(authConfigurationProperties).setCookie("token", httpServletResponse);
        (authConfigurationProperties).setCookie("token", httpServletResponse);
        when(authConfigurationProperties.getJwtSecret()).thenReturn(key);
        Assert.assertNotNull(tokenService.login(new LoginRequest("", ""), httpServletRequest, httpServletResponse));
    }

    @Test
    public void verifyLoginWithNoPassword() throws ServletException {
        ReflectionTestUtils.setField(authConfigurationProperties, "cookieTokenName", "zoweSdkAuthenticationToken");
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic "
            + Base64.getEncoder().encodeToString(("&").getBytes()));
        when(authConfigurationProperties.getCredentialFromAuthorizationHeader(httpServletRequest)).thenCallRealMethod().
            thenReturn(java.util.Optional.ofNullable(loginRequest));
        when(authConfigurationProperties.mapBase64Credentials(any())).thenCallRealMethod().
            thenReturn(null);
        Mockito.doCallRealMethod().when(authConfigurationProperties).setCookie("token", httpServletResponse);
        (authConfigurationProperties).setCookie("token", httpServletResponse);

        Assert.assertNull(tokenService.login(new LoginRequest("", ""), httpServletRequest, httpServletResponse));
    }

    @Test
    public void throwZosAuthenticationException() throws ServletException, IOException {
        when(zoweAuthenticationFailureHandler.handleException(any(), any())).thenCallRealMethod().thenReturn(true);
        File f = File.createTempFile("test", null);
        f.deleteOnExit();
        ;
        when(httpServletResponse.getWriter()).thenReturn(new PrintWriter(f.getAbsolutePath()));
        Assert.assertNull(tokenService.login(new LoginRequest("", ""), httpServletRequest, httpServletResponse));
    }

    @Test
    public void throwAuthenticationCredsNotFoundException() throws ServletException, IOException {
        when(zoweAuthenticationFailureHandler.handleException(any(), any())).thenCallRealMethod().thenReturn(true);
        File f = File.createTempFile("test", null);
        f.deleteOnExit();
        ;
        when(httpServletResponse.getWriter()).thenReturn(new PrintWriter(f.getAbsolutePath()));
        Assert.assertNull(tokenService.login(any(), httpServletRequest, httpServletResponse));
    }

    @Test
    public void verifyQuery() throws ServletException {
        Cookie tokenCookie = new Cookie("zoweSdkAuthenticationToken", token);
        tokenCookie.setComment("Zowe SDK security token");
        tokenCookie.setPath("/");

        Cookie[] cookies = new Cookie[1];
        cookies[0] = tokenCookie;

        when(authConfigurationProperties.getCookieTokenName()).thenCallRealMethod().thenReturn("zoweSdkAuthenticationToken");
        when(authConfigurationProperties.getClaims(token)).thenCallRealMethod().thenReturn(new QueryResponse());
        when(httpServletRequest.getCookies()).thenReturn(cookies);
        when(authConfigurationProperties.getJwtSecret()).thenReturn(key);

        Assert.assertNotNull(tokenService.query(httpServletRequest));
    }

    @Test
    public void verifyInvalidTokenInQueryAPI() {
        Cookie tokenCookie = new Cookie("zoweSdkAuthenticationToken", token);
        tokenCookie.setComment("Zowe SDK security token");
        tokenCookie.setPath("/");

        Cookie[] cookies = new Cookie[1];
        cookies[0] = tokenCookie;

        when(httpServletRequest.getCookies()).thenReturn(cookies);

        Assert.assertNull(tokenService.query(httpServletRequest));
    }

    @Test
    public void validateToken() {
        when(authConfigurationProperties.getJwtSecret()).thenReturn(key);
        when(authConfigurationProperties.getClaims(token)).thenCallRealMethod().thenReturn(new QueryResponse());
        Assert.assertTrue(tokenService.validateToken(token));
    }

    @Test
    public void validateInvalidToken() {
        Assert.assertFalse(tokenService.validateToken("token"));
    }

}
