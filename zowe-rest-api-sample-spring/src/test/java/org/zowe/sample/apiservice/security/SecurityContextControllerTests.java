/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.security;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.zowe.commons.spring.config.ZoweAuthenticationUtility;
import org.zowe.sample.apiservice.TestUtils;

import javax.servlet.http.Cookie;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(SecurityContextController.class)
public class SecurityContextControllerTests {

    private ZoweAuthenticationUtility zoweAuthenticationUtility =
        new ZoweAuthenticationUtility();

    @Autowired
    private MockMvc mvc;

    String token = null;

    @Value("${zowe.commons.security.token.cookieTokenName:zoweSdkAuthenticationToken}")
    private String cookieTokenName;

    @Before
    public void setup() throws Exception {
        MvcResult loginResult = this.mvc.perform(post("/api/v1/auth/login").
            header("Authorization", TestUtils.ZOWE_BASIC_AUTHENTICATION)).andExpect(status().isNoContent()).andReturn();

        Cookie[] cookies = loginResult.getResponse().getCookies();
        if (cookies != null) {
            token = zoweAuthenticationUtility.BEARER_AUTHENTICATION_PREFIX + Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(cookieTokenName))
                .filter(cookie -> !cookie.getValue().isEmpty())
                .findFirst().get().getValue();
        }
    }

    @Test
    public void returnsDataAboutSwitchedContext() throws Exception {
        mvc.perform(get("/api/v1/securityTest/authenticatedUser")
            .header("Authorization", token).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andExpect(jsonPath("$.afterSwitchUserName", is("zowe")));
    }

    @Test
    public void failsWithoutAuthentication() throws Exception {
        mvc.perform(get("/api/v1/securityTest/authenticatedUser")).andExpect(status().isUnauthorized());
    }

    @Ignore
    public void failsWithInvalidAuthentication() throws Exception {
        String invalidToken = "eyJhbGciOiJIUzUxMiJ9.fyryerytuytry.KILjk1gpVxLY1wrr8";
        mvc.perform(get("/api/v1/securityTest/authenticatedUser").header("Authorization",
            invalidToken)).andDo(print()).andExpect(status().isUnauthorized());
    }

    @Test
    public void allowsRequestToPermittedResource() throws Exception {
        mvc.perform(get("/api/v1/securityTest/safProtectedResource").header("Authorization",
            token)).andDo(print()).andExpect(status().isOk())
            .andExpect(jsonPath("$.canMount", is("true")));
    }

    @Test
    public void forbidsRequestToDeniedResource() throws Exception {
        mvc.perform(get("/api/v1/securityTest/safDeniedResource").header("Authorization",
            token)).andDo(print()).andExpect(status().isForbidden());
    }

}
