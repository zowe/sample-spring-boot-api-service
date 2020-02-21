/*
 * This program and the accompanying materials are made available under the terms of the
 * Apache License, Version 2.0 which accompanies this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.greeting;

import org.junit.Test;
import org.zowe.sample.apiservice.test.IntegrationTests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class GreetingControllerIntegrationTests extends IntegrationTests {

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
