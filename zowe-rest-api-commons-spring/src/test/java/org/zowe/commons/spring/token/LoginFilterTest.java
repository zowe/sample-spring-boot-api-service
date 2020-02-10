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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.zowe.commons.spring.config.ZoweAuthenticationUtility;
import org.zowe.commons.spring.login.LoginFilter;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationProvider;

import javax.servlet.ServletException;
import javax.ws.rs.HttpMethod;
import java.io.IOException;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LoginFilterTest {
    private static final String VALID_JSON = "{\"username\": \"user\", \"password\": \"pwd\"}";
    private static final String EMPTY_JSON = "{\"username\": \"\", \"password\": \"\"}";
    private static final String VALID_AUTH_HEADER = "Basic dXNlcjpwd2Q=";
    private static final String INVALID_AUTH_HEADER = "Basic dXNlcj11c2Vy";

    private MockHttpServletRequest httpServletRequest;
    private MockHttpServletResponse httpServletResponse;
    private LoginFilter loginFilter;
    private final ObjectMapper mapper = new ObjectMapper();
    private ZoweAuthenticationUtility authConfigurationProperties = new ZoweAuthenticationUtility();

    @Mock
    private TokenService tokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    ZosAuthenticationProvider authenticationProvider;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        loginFilter = new LoginFilter("TEST_ENDPOINT",
            authConfigurationProperties,
            authenticationManager,
            tokenService);
    }

    @Ignore
    public void shouldCallAuthenticationManagerAuthenticateWithAuthHeader() throws ServletException, IOException {
        httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setMethod(HttpMethod.POST);
        httpServletRequest.addHeader(HttpHeaders.AUTHORIZATION, VALID_AUTH_HEADER);
        httpServletResponse = new MockHttpServletResponse();

        loginFilter.attemptAuthentication(httpServletRequest, httpServletResponse);

        UsernamePasswordAuthenticationToken authentication
            = new UsernamePasswordAuthenticationToken("user", "pwd");

        verify(authenticationProvider).authenticate(authentication);
    }

    @Ignore
    public void shouldCallAuthenticationManagerAuthenticateWithJson() throws ServletException, IOException {
        httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setMethod(HttpMethod.POST);
        httpServletRequest.setContent(VALID_JSON.getBytes());
        httpServletResponse = new MockHttpServletResponse();

        loginFilter.attemptAuthentication(httpServletRequest, httpServletResponse);

        UsernamePasswordAuthenticationToken authentication
            = new UsernamePasswordAuthenticationToken("user", "pwd");
        verify(authenticationManager).authenticate(authentication);
    }

    @Test
    public void shouldFailWithJsonEmptyCredentials() throws ServletException, IOException {
        httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setMethod(HttpMethod.POST);
        httpServletRequest.setContent(EMPTY_JSON.getBytes());
        httpServletResponse = new MockHttpServletResponse();

        exception.expect(AuthenticationCredentialsNotFoundException.class);
        exception.expectMessage("Username or password not provided.");

        loginFilter.attemptAuthentication(httpServletRequest, httpServletResponse);
    }

    @Test
    public void shouldFailWithoutAuth() throws ServletException, IOException {
        httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setMethod(HttpMethod.POST);
        httpServletResponse = new MockHttpServletResponse();

        exception.expect(AuthenticationCredentialsNotFoundException.class);
        exception.expectMessage("Login object has wrong format.");

        loginFilter.attemptAuthentication(httpServletRequest, httpServletResponse);
    }

    @Test
    public void shouldFailWithIncorrectCredentialsFormat() throws ServletException, IOException {
        httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setMethod(HttpMethod.POST);
        httpServletRequest.addHeader(HttpHeaders.AUTHORIZATION, INVALID_AUTH_HEADER);
        httpServletResponse = new MockHttpServletResponse();

        exception.expect(AuthenticationCredentialsNotFoundException.class);
        exception.expectMessage("Login object has wrong format.");

        loginFilter.attemptAuthentication(httpServletRequest, httpServletResponse);
    }
}
