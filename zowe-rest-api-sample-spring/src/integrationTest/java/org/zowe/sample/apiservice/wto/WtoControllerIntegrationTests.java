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

import org.junit.Test;
import org.zowe.sample.apiservice.test.IntegrationTests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class WtoControllerIntegrationTests extends IntegrationTests {

    @Test
    public void returnsWtoMessage_Token() throws Exception {
        given().header("Authorization", token).when().get("/api/v1/wto").then().statusCode(200).body("content", equalTo("Hello, world!")).body("message",
            not(isEmptyOrNullString()));
    }

    @Test
    public void returnsWtoMessage_Cookie() throws Exception {
        given().cookie(cookieName, token.split(" ")[1]).when().get("/api/v1/wto").then().statusCode(200).body("content", equalTo("Hello, world!")).body("message",
            not(isEmptyOrNullString()));
    }
}
