/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.security;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zowe.sample.apiservice.test.ServiceUnderTest;

public class SecurityContextControllerIntegrationTests {
    @BeforeClass
    public static void setup() {
        ServiceUnderTest.getInstance().waitUntilIsReady();
    }

    @Test
    public void switchesContextToAuthenticatedUserId() throws Exception {
        Matcher<String> equalsToAuthenticatedUserID = equalTo(ServiceUnderTest.getInstance().getUserId());
        when().get("/api/v1/securityTest/authenticatedUser").then().statusCode(200)
                .body("afterSwitchUserName", equalsToAuthenticatedUserID)
                .body("afterSwitchUserNameSpring", equalsToAuthenticatedUserID)
                .body("authenticatedUserName", equalsToAuthenticatedUserID);
    }
}
