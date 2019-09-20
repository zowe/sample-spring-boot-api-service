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

import static io.restassured.RestAssured.when;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.containsString;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.zowe.sample.apiservice.test.ServiceUnderTest;

public class GreetingControllerIntegrationTests {
    @BeforeClass
    public static void setup() {
        ServiceUnderTest.getInstance().waitUntilIsReady();
    }

    @Test
    public void returnsGreeting() throws Exception {
        when().get("/api/v1/greeting").then().statusCode(200).body("content", equalTo("Hello, world!"));
    }

    @Test
    public void failsWithoutAuthentication() throws Exception {
        given().auth().none().when().get("/api/v1/greeting").then().statusCode(401)
                .headers(HttpHeaders.WWW_AUTHENTICATE, containsString("Basic realm=\"Zowe Sample API Service\""));
    }
}
