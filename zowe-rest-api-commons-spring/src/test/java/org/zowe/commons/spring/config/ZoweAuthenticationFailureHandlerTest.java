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

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.client.ResourceAccessException;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ZoweAuthenticationFailureHandlerTest {

    private ZoweAuthenticationFailureHandler zoweAuthenticationFailureHandler = new ZoweAuthenticationFailureHandler();

    private HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);

    private ExpiredJwtException expiredJwtException = mock(ExpiredJwtException.class);

    private ZosAuthenticationException zosAuthenticationException = mock(ZosAuthenticationException.class);

    @Before
    public void setup() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(output));
        when(httpServletResponse.getWriter()).thenReturn(new PrintWriter(printWriter));
    }

    @Test
    public void checkSignatureException() throws ServletException {
        assertFalse(zoweAuthenticationFailureHandler.handleException(new SignatureException(""), httpServletResponse));
        assertFalse(zoweAuthenticationFailureHandler.handleException(new InsufficientAuthenticationException(""), httpServletResponse));
        assertFalse(zoweAuthenticationFailureHandler.handleException(new BadCredentialsException(""), httpServletResponse));
        assertFalse(zoweAuthenticationFailureHandler.handleException(new AuthenticationCredentialsNotFoundException(""), httpServletResponse));
        assertFalse(zoweAuthenticationFailureHandler.handleException(new NullPointerException(""), httpServletResponse));
        assertFalse(zoweAuthenticationFailureHandler.handleException(expiredJwtException, httpServletResponse));
        assertFalse(zoweAuthenticationFailureHandler.handleException(zosAuthenticationException, httpServletResponse));
        assertFalse(zoweAuthenticationFailureHandler.handleException(new ResourceAccessException(""), httpServletResponse));
    }

    @Test(expected = InsufficientAuthenticationException.class)
    public void checkGeneralException() throws ServletException {
        zoweAuthenticationFailureHandler.handleException(new RuntimeException(""), httpServletResponse);
    }

    @Test
    public void testLocalizedMessage() {
        assertNotNull(zoweAuthenticationFailureHandler.localizedMessage("org.zowe.commons.rest.invalidToken"));
        assertNotNull(zoweAuthenticationFailureHandler.localizedMessage("org.zowe.commons.rest.forbidden","sample message"));
    }

}
