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

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.VALID_PASSWORD;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.VALID_USERID;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.zowe.sample.apiservice.test.IntegrationTests;

import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Base64;

public class GreetingControllerIntegrationTests extends IntegrationTests {

    String token = null;

    @Before
    public void setup() {
        token = serviceUnderTest.login();
    }

    @Test
    public void returnsGreeting() throws Exception {
        given().header("Authorization", token).when()
            .get("/api/v1/greeting").
            then().statusCode(200).body("content", equalTo("Hello, world!"));
    }

    @Test
    public void failsWithoutAuthentication() throws Exception {
        given().auth().none().when().get("/api/v1/greeting").then().statusCode(401);
    }
}
