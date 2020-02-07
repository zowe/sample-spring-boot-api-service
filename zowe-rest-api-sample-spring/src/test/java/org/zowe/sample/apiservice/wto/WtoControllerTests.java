/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.wto;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.zowe.sample.apiservice.TestUtils;

import javax.servlet.http.Cookie;
import java.util.Arrays;

@RunWith(SpringRunner.class)
@WebMvcTest(WtoController.class)
public class WtoControllerTests {

    @Autowired
    private MockMvc mvc;

    String token = null;

    @Before
    public void setup() throws Exception {
        MvcResult loginResult = this.mvc.perform(get("/api/v1/auth/login").header("Authorization", TestUtils.ZOWE_BASIC_AUTHENTICATION)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent()).andReturn();

        Cookie[] cookies = loginResult.getResponse().getCookies();
        if (cookies != null) {
            token = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("zoweSdkAuthenticationToken"))
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
