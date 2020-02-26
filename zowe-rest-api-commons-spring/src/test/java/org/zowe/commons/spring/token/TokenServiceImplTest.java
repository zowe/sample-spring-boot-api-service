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
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.zowe.commons.spring.config.ZoweAuthenticationFailureHandler;
import org.zowe.commons.spring.config.ZoweAuthenticationUtility;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationProvider;

import javax.management.Query;
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
    public void initMocks() throws IOException {
        MockitoAnnotations.initMocks(this);
        when(zosAuthenticationProvider.authenticate(authenticationToken)).thenReturn(authenticationToken);
    }

    @Test
    public void verifyLogin() throws ServletException {
        tokenService.login(loginRequest, httpServletRequest, httpServletResponse);
    }

    @Test
    public void verifyLoginWithBasic() throws ServletException {
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic "
            + Base64.getEncoder().encodeToString(("zowe" + ":" + "zowe").getBytes()));
        when(authConfigurationProperties.getCredentialFromAuthorizationHeader(httpServletRequest)).thenCallRealMethod().
            thenReturn(java.util.Optional.ofNullable(loginRequest));

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
        Cookie tokenCookie = new Cookie("zoweSdkAuthenticationToken", "token");
        tokenCookie.setComment("Zowe SDK security token");
        tokenCookie.setPath("/");

        Cookie[] cookies = new Cookie[1];
        cookies[0] = tokenCookie;

        when(httpServletRequest.getCookies()).thenReturn(cookies);

        tokenService.query(httpServletRequest);
    }

    @Test(expected = Exception.class)
    public void expiredTokenQueryApi() throws ServletException {
        String token = Jwts.builder()
            .setSubject("zowe")
            .setExpiration(new Date(System.currentTimeMillis() + 10000))
            .signWith(SignatureAlgorithm.HS512, "secretKey")
            .compact();

        Cookie tokenCookie = new Cookie("zoweSdkAuthenticationToken", token);
        Cookie[] cookies = new Cookie[1];
        cookies[0] = tokenCookie;

        when(authConfigurationProperties.getClaims(token)).thenCallRealMethod().
            thenReturn(new QueryResponse());
        when(authConfigurationProperties.getCookieTokenName()).thenReturn("zoweSdkAuthenticationToken");
        when(httpServletRequest.getCookies()).thenReturn(cookies);
        tokenService.query(httpServletRequest);
    }
}
