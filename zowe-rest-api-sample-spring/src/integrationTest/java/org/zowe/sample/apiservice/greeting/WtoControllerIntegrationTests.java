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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

import org.junit.BeforeClass;
import org.junit.Test;
import org.zowe.sample.apiservice.test.ServiceUnderTest;

public class WtoControllerIntegrationTests {
    @BeforeClass
    public static void setup() {
        ServiceUnderTest.getInstance().waitUntilIsReady();
    }

    @Test
    public void returnsWtoMessage() throws Exception {
        when().get("/api/v1/wto").then().statusCode(200).body("content", equalTo("Hello, world!")).body("message",
                not(isEmptyOrNullString()));
    }
}
