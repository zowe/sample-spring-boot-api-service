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

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    private LoginRequest loginRequest = new LoginRequest("zowe", "zowe");
    private UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        when(zosAuthenticationProvider.authenticate(authenticationToken)).thenReturn(authenticationToken);
        ReflectionTestUtils.setField(authConfigurationProperties, "secretKey", "secretKey");
        ReflectionTestUtils.setField(authConfigurationProperties, "cookieTokenName", "zoweSdkAuthenticationToken");
    }

    @Test
    public void verifyLogin() throws ServletException {
        tokenService.login(loginRequest, httpServletRequest, httpServletResponse);
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

        tokenService.login(new LoginRequest("", ""), httpServletRequest, httpServletResponse);
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

        tokenService.login(new LoginRequest("", ""), httpServletRequest, httpServletResponse);
    }

    @Test
    public void throwZosAuthenticationException() throws ServletException, IOException {
        when(zoweAuthenticationFailureHandler.handleException(any(), any())).thenCallRealMethod().thenReturn(true);
        when(httpServletResponse.getWriter()).thenReturn(new PrintWriter("writer"));
        tokenService.login(new LoginRequest("", ""), httpServletRequest, httpServletResponse);
    }

    @Test
    public void throwAuthenticationCredsNotFoundException() throws ServletException, IOException {
        when(zoweAuthenticationFailureHandler.handleException(any(), any())).thenCallRealMethod().thenReturn(true);
        when(httpServletResponse.getWriter()).thenReturn(new PrintWriter("writer"));
        tokenService.login(any(), httpServletRequest, httpServletResponse);
    }

    @Test
    public void verifyQuery() throws ServletException {
        String token = Jwts.builder()
            .setSubject("zowe")
            .signWith(SignatureAlgorithm.HS512, "secretKey")
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .compact();

        Cookie tokenCookie = new Cookie("zoweSdkAuthenticationToken", token);
        tokenCookie.setComment("Zowe SDK security token");
        tokenCookie.setPath("/");

        Cookie[] cookies = new Cookie[1];
        cookies[0] = tokenCookie;

        when(authConfigurationProperties.getCookieTokenName()).thenCallRealMethod().thenReturn("zoweSdkAuthenticationToken");
        when(authConfigurationProperties.getClaims(token)).thenCallRealMethod().thenReturn(new QueryResponse());
        when(httpServletRequest.getCookies()).thenReturn(cookies);

        tokenService.query(httpServletRequest);
    }

}
