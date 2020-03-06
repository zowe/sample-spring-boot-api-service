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
