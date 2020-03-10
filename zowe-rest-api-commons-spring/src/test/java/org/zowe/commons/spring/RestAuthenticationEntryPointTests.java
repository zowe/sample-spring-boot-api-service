/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.spring;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.junit4.SpringRunner;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationProvider;
import org.zowe.commons.zos.security.platform.PlatformPwdErrno;
import org.zowe.commons.zos.security.platform.PlatformReturned;

@RunWith(SpringRunner.class)
public class RestAuthenticationEntryPointTests {
    @Test
    public void handleGeneralAuthenticationException() throws IOException, ServletException {
        RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        entryPoint.commence(request, response, new AuthenticationException("failed") {
            private static final long serialVersionUID = 1L;
        });
        assertEquals(response.getStatus(), HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void handlePlatformAuthenticationWithoutValidCredentials() throws IOException, ServletException {
        RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest();
        PlatformReturned.PlatformReturnedBuilder builder = PlatformReturned.builder().success(false);
        request.setAttribute(ZosAuthenticationProvider.ZOWE_AUTHENTICATE_RETURNED, builder.errno(PlatformPwdErrno.EACCES.errno).build());
        MockHttpServletResponse response = new MockHttpServletResponse();
        entryPoint.commence(request, response, new AuthenticationException("failed") {
            private static final long serialVersionUID = 1L;
        });
        assertEquals(response.getStatus(), HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void handlePlatformAuthenticationException() throws IOException, ServletException {
        RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest();
        PlatformReturned.PlatformReturnedBuilder builder = PlatformReturned.builder().success(false);
        request.setAttribute(ZosAuthenticationProvider.ZOWE_AUTHENTICATE_RETURNED, builder.errno(PlatformPwdErrno.EMVSEXPIRE.errno).build());
        MockHttpServletResponse response = new MockHttpServletResponse();
        entryPoint.commence(request, response, new AuthenticationException("failed") {
            private static final long serialVersionUID = 1L;
        });
        assertEquals(response.getStatus(), HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void handlePlatformAuthenticationInternalError() throws IOException, ServletException {
        RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest();
        PlatformReturned.PlatformReturnedBuilder builder = PlatformReturned.builder().success(false);
        request.setAttribute(ZosAuthenticationProvider.ZOWE_AUTHENTICATE_RETURNED, builder.errno(PlatformPwdErrno.EPERM.errno).build());
        MockHttpServletResponse response = new MockHttpServletResponse();
        entryPoint.commence(request, response, new AuthenticationException("failed") {
            private static final long serialVersionUID = 1L;
        });
        assertEquals(response.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
