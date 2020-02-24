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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.zowe.commons.spring.config.ZoweAuthenticationUtility;
import org.zowe.commons.spring.login.LoginRequest;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TokenServiceImplTest {

    @InjectMocks
    TokenServiceImpl tokenService;

    @Mock
    HttpServletResponse httpServletResponse;

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    ZosAuthenticationProvider zosAuthenticationProvider;

    @Mock
    ZoweAuthenticationUtility authConfigurationProperties;

    LoginRequest loginRequest = new LoginRequest("zowe", "zowe");

    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

    @Before
    public void initMocks() {

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

        tokenService.login(new LoginRequest("", ""), httpServletRequest, httpServletResponse);
    }
}
