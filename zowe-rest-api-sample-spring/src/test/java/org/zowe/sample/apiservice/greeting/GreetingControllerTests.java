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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.zowe.commons.spring.config.ZoweAuthenticationUtility;
import org.zowe.sample.apiservice.TestUtils;

import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Locale;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(GreetingController.class)
public class GreetingControllerTests {

    private ZoweAuthenticationUtility zoweAuthenticationUtility =
        new ZoweAuthenticationUtility();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MessageSource messageSource;

    @Value("${zowe.commons.security.token.cookieTokenName:zoweSdkAuthenticationToken}")
    private String cookieTokenName;

    String token = null;

    @Before
    public void setup() throws Exception {
        MvcResult loginResult = this.mvc.perform(post("/api/v1/auth/login").
            header("Authorization", TestUtils.ZOWE_BASIC_AUTHENTICATION)).andExpect(status().isOk()).andReturn();

        Cookie[] cookies = loginResult.getResponse().getCookies();
        if (cookies != null) {
            token = zoweAuthenticationUtility.bearerAuthenticationPrefix + Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(cookieTokenName))
                .filter(cookie -> !cookie.getValue().isEmpty())
                .findFirst().get().getValue();
        }
    }

    @Test
    public void returnsGreetingSpring() throws Exception {

        MvcResult mvcResult = this.mvc.perform(get("/api/v1/greeting")
            .header("Authorization", token)
            .accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("Hello, world!"))
            .andReturn();

        Assert.assertEquals("application/json;charset=UTF-8",
            mvcResult.getResponse().getContentType());

    }

    @Test
    public void returnsGreetingSpringLocaleParam() throws Exception {
        mvc.perform(get("/api/v1/greeting?lang=cs").header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
            .andExpect(jsonPath("$.content", is("Ahoj, světe!")));
    }

    @Test
    public void returnsGreetingSpringLocaleHeader() throws Exception {
        mvc.perform(get("/api/v1/greeting").header("Authorization", token)
            .locale(new Locale("cs")).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
            .andExpect(jsonPath("$.content", is("Ahoj, světe!")));
    }

    @Test
    public void returnsGreetingRestAssured() throws Exception {
        GreetingController greetingController = new GreetingController();
        greetingController.setMessageSource(messageSource);
        given().standaloneSetup(greetingController).auth().principal(TestUtils.ZOWE_AUTHENTICATION_TOKEN).when()
            .get("/api/v1/greeting").then().body("$.content", equalTo("Hello, world!"));
    }

    @Test
    public void emptyNameFails() throws Exception {
        mvc.perform(get("/api/v1/greeting").header("Authorization", token)
            .param("name", " ").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.messages[0].messageKey", is("org.zowe.sample.apiservice.greeting.empty")));
    }

    @Test
    public void failsWithoutAuthentication() throws Exception {
        mvc.perform(get("/api/v1/greeting")).andExpect(status().isUnauthorized());
    }

    @Test
    public void exceptionCausesFailure() throws Exception {
        mvc.perform(get("/api/v1/greeting/failed").header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isInternalServerError());
    }

    @Test
    public void settingsCanBeUpdated() throws Exception {
        GreetingSettings settings = new GreetingSettings();
        ObjectMapper mapper = new ObjectMapper();
        settings.setGreeting("Hi");
        mvc.perform(put("/api/v1/greeting/settings").header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(settings)))
            .andExpect(status().isOk());
    }
}
