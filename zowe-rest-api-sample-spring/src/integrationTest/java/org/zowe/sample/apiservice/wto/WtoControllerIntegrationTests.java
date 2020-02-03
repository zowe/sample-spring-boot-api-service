/*
 * This program and the accompanying materials are made available under the terms of the
 * Apache License, Version 2.0 which accompanies this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.wto;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

import org.junit.Test;
import org.zowe.sample.apiservice.test.IntegrationTests;

public class WtoControllerIntegrationTests extends IntegrationTests {
    @Test
    public void returnsWtoMessage() throws Exception {
        when().get("/api/v1/wto").then().statusCode(200).body("content", equalTo("Hello, world!")).body("message",
                not(isEmptyOrNullString()));
    }
}
