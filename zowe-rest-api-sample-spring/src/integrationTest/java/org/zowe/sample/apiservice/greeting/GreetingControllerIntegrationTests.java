/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.greeting;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.zowe.sample.apiservice.test.IntegrationTests;

public class GreetingControllerIntegrationTests extends IntegrationTests {
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
