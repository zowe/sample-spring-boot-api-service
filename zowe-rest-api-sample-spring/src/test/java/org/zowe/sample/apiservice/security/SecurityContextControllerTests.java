/*
 * This program and the accompanying materials are made available under the terms of the
 * Apache License, Version 2.0 which accompanies this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.security;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.zowe.sample.apiservice.TestUtils;

@RunWith(SpringRunner.class)
@WebMvcTest(SecurityContextController.class)
public class SecurityContextControllerTests {

    @Autowired
    private MockMvc mvc;

    @Test
    public void returnsDataAboutSwitchedContext() throws Exception {
        mvc.perform(get("/api/v1/securityTest/authenticatedUser")
                .header("Authorization", TestUtils.ZOWE_BASIC_AUTHENTICATION).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.afterSwitchUserName", is("zowe")));
    }

    @Test
    public void failsWithoutAuthentication() throws Exception {
        mvc.perform(get("/api/v1/securityTest/authenticatedUser")).andExpect(status().isUnauthorized());
    }

    @Test
    public void failsWithInvalidAuthentication() throws Exception {
        mvc.perform(get("/api/v1/securityTest/authenticatedUser").header("Authorization",
                TestUtils.ZOWE_BASIC_AUTHENTICATION_INVALID)).andDo(print()).andExpect(status().isUnauthorized());
    }

    @Test
    public void allowsRequestToPermittedResource() throws Exception {
        mvc.perform(get("/api/v1/securityTest/safProtectedResource").header("Authorization",
                TestUtils.ZOWE_BASIC_AUTHENTICATION)).andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.canMount", is("true")));
    }

    @Test
    public void forbidsRequestToDeniedResource() throws Exception {
        mvc.perform(get("/api/v1/securityTest/safDeniedResource").header("Authorization",
                TestUtils.ZOWE_BASIC_AUTHENTICATION)).andDo(print()).andExpect(status().isForbidden());
    }

}
