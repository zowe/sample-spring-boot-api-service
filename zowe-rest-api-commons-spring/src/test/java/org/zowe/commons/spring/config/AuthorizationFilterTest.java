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

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizationFilterTest {

    private static final String SECRET_KEY = "8Zz5tw0Ionm3XPZZfN0NOml3z9FMfmpgXwovR9fp6ryDIoGRM8EPHAB6iHsc0fb";

    @InjectMocks
    private static AuthorizationFilter authorizationFilter;

    @Mock
    private static ZoweAuthenticationUtility zoweAuthenticationUtility;

    @Mock
    HttpServletRequest httpServletRequest;

    Cookie[] cookies = new Cookie[1];

    private Cookie[] createCookie() {

        cookies = new Cookie[1];

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
        long expiredTimeMillis = System.currentTimeMillis() + 10000;

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
    public void checkWhenCookiesIsNull() {
        when(httpServletRequest.getCookies()).thenReturn(null);
        assertNotNull(authorizationFilter.extractContent(httpServletRequest));
    }
}
