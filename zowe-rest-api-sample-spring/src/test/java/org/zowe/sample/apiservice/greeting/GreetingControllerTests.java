/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.greeting;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.zowe.sample.apiservice.TestUtils;

@RunWith(SpringRunner.class)
@WebMvcTest(GreetingController.class)
public class GreetingControllerTests {

    @Autowired
    private MockMvc mvc;

    @Test
    public void returnsGreetingSpring() throws Exception {
        mvc.perform(get("/api/v1/greeting").header("Authorization", TestUtils.ZOWE_BASIC_AUTHENTICATION)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("Hello, world!")));
    }

    @Test
    public void returnsGreetingRestAssured() throws Exception {
        given().standaloneSetup(new GreetingController()).auth().principal(TestUtils.ZOWE_AUTHENTICATION_TOKEN).when()
                .get("/api/v1/greeting").then().body("$.content", equalTo("Hello, world!"));
    }

    @Test
    public void emptyNameFails() throws Exception {
        mvc.perform(get("/api/v1/greeting").header("Authorization", TestUtils.ZOWE_BASIC_AUTHENTICATION)
                .param("name", " ").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0].messageKey", is("org.zowe.sample.apiservice.greeting.empty")));
    }

    @Test
    public void failsWithoutAuthentication() throws Exception {
        mvc.perform(get("/api/v1/greeting")).andExpect(status().isUnauthorized())
                .andExpect(header().exists(HttpHeaders.WWW_AUTHENTICATE));
    }

    @Test
    public void exceptionCausesFailure() throws Exception {
        mvc.perform(get("/api/v1/greeting/failed").header("Authorization", TestUtils.ZOWE_BASIC_AUTHENTICATION)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isInternalServerError());
    }
}
