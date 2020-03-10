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

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.zowe.sample.apiservice.test.IntegrationTests;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class GreetingControllerIntegrationTests extends IntegrationTests {

    @Test
    public void returnsGreetingUsingBearerToken() throws Exception {
        given().header("Authorization", token).when()
            .get("/api/v1/greeting").
            then().statusCode(200).body("content", equalTo("Hello, world!"));
    }

    @Ignore
    @Test
    public void greetingFailsWithoutAuthenticationUsingEmptyToken() throws Exception {
        given().header("Authorization", "").when().get("/api/v1/greeting").then().statusCode(401);
    }

    @Test
    public void greetingFailsWithoutAuthenticationUsingIncorrectToken() throws Exception {
        given().header("Authorization", token+"Extra").when().get("/api/v1/greeting").then().statusCode(401);
    }

    @Test
    public void returnsGreetingUsingCookie() throws Exception {
        given().cookie(cookieName, token.split(" ")[1]).when()
            .get("/api/v1/greeting").
            then().statusCode(200).body("content", equalTo("Hello, world!"));
    }

    @Test
    public void returnsGreetingUsingBasicauth() throws Exception {
        when().get("/api/v1/greeting").then().statusCode(200).body("content", equalTo("Hello, world!"));
    }

    @Test
    public void greetingFailsWithoutAuthenticationUsingBasicAuth() throws Exception {
        given().auth().none().when().get("/api/v1/greeting").then().statusCode(401)
            .headers(HttpHeaders.WWW_AUTHENTICATE, containsString("Basic realm=\"Zowe Sample API Service\""));
    }
}
