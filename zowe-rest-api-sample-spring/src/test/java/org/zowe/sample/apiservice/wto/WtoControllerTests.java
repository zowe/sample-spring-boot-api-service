/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.wto;

import org.junit.Before;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(WtoController.class)
public class WtoControllerTests {

    private ZoweAuthenticationUtility zoweAuthenticationUtility =
        new ZoweAuthenticationUtility();

    @Autowired
    private MockMvc mvc;

    @Value("${zowe.commons.security.token.cookieTokenName:zoweSdkAuthenticationToken}")
    private String cookieTokenName;

    String token = null;

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
    public void returnsWtoMessage() throws Exception {
        mvc.perform(get("/api/v1/wto").header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
            .andExpect(jsonPath("$.content", is("Hello, world!")))
            .andExpect(jsonPath("$.message", is("[Mock] Message set from JNI")));
    }

    @Test
    public void failsWithoutAuthentication() throws Exception {
        mvc.perform(get("/api/v1/wto")).andExpect(status().isUnauthorized());
    }
}
