/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.spring.query;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.zowe.commons.error.TokenExpireException;
import org.zowe.commons.error.TokenNotValidException;
import org.zowe.commons.spring.config.ZoweAuthenticationUtility;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QueryServiceTest {

    private static final SignatureAlgorithm ALGORITHM = SignatureAlgorithm.HS512;
    private static final String USER = "zowe";
    private static final String SECRET_KEY = "8Zz5tw0Ionm3XPZZfN0NOml3z9FMfmpgXwovR9fp6ryDIoGRM8EPHAB6iHsc0fb";

    @InjectMocks
    private static QueryService queryService;

    @Mock
    private static ZoweAuthenticationUtility zoweAuthenticationUtility;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        when(zoweAuthenticationUtility.getSecretKey()).thenReturn(SECRET_KEY);
    }

    private String createExpiredJwtToken(String secretKey) {
        long expiredTimeMillis = System.currentTimeMillis() - 1000;

        return Jwts.builder()
            .setExpiration(new Date(expiredTimeMillis))
            .signWith(ALGORITHM, secretKey)
            .compact();
    }

    private String createJwtToken(String secretKey) {
        long expiredTimeMillis = System.currentTimeMillis() + 10000;

        return Jwts.builder()
            .setSubject(USER)
            .setExpiration(new Date(expiredTimeMillis))
            .signWith(ALGORITHM, secretKey)
            .compact();
    }

    @Test
    public void shouldValidateCorrectToken() {
        String token = createJwtToken(SECRET_KEY);
        Claims claims = queryService.getClaims(token);
        assertEquals("zowe", claims.getSubject());
    }

    @Test(expected = TokenNotValidException.class)
    public void shouldGiveTokenNotValidException() {
        Claims claims = queryService.getClaims(createJwtToken(SECRET_KEY).replace(".", ","));
        assertEquals("zowe", claims.getSubject());
    }

    @Test(expected = TokenExpireException.class)
    public void shouldGiveExpiredTokenException() {
        String token = createExpiredJwtToken(SECRET_KEY);
        Claims claims = queryService.getClaims(token);
    }

}

