/*
 * This program and the accompanying materials are made available under the terms of the
 * Apache License, Version 2.0 which accompanies this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.security;

import static io.restassured.RestAssured.basic;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.Assume.assumeTrue;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.EXPIRED_PASSWORD;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.FAILING_PASSWORD;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.INVALID_PASSWORD;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.INVALID_USERID;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.VALID_USERID;
import static org.zowe.sample.apiservice.test.ServiceUnderTest.LOCAL_PROFILE;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.zowe.sample.apiservice.test.IntegrationTests;

import io.restassured.RestAssured;

public class SecurityContextControllerIntegrationTests extends IntegrationTests {
    @Test
    public void switchesContextToAuthenticatedUserId() throws Exception {
        serviceUnderTest.defaultRestAssuredSetup();
        Matcher<String> equalsToAuthenticatedUserID = equalToIgnoringCase(serviceUnderTest.getUserId());
        when().get("/api/v1/securityTest/authenticatedUser").then().statusCode(200)
                .body("afterSwitchUserName", equalsToAuthenticatedUserID)
                .body("afterSwitchUserNameSpring", equalsToAuthenticatedUserID)
                .body("authenticatedUserName", equalsToAuthenticatedUserID)
                .body("accessToBpxServerServer", equalTo("true"))
                .body("accessToUndefinedResource", equalTo("false"))
                .body("accessToUndefinedResourceAllowMissingResource", equalTo("true"));
    }

    @Test
    public void failsWithInvalidAuthentication() throws Exception {
        RestAssured.authentication = basic(INVALID_USERID, INVALID_PASSWORD);
        when().get("/api/v1/securityTest/authenticatedUser").then().statusCode(401);
    }

    @Test
    public void failsWithEmptyPassword() throws Exception {
        RestAssured.authentication = basic(VALID_USERID, "");
        when().get("/api/v1/securityTest/authenticatedUser").then().statusCode(401);
    }

    @Test
    public void failsWithExpiredAuthentication() throws Exception {
        assumeTrue("The service under test is not running on localhost",
                LOCAL_PROFILE.equalsIgnoreCase(serviceUnderTest.getProfile()));

        RestAssured.authentication = basic(VALID_USERID, EXPIRED_PASSWORD);
        when().get("/api/v1/securityTest/authenticatedUser").then().statusCode(401)
                .body("messages[0].messageContent", containsString("expired"))
                .body("messages[0].messageReason", containsString("missing valid authentication credentials"))
                .body("messages[0].messageAction", containsString("contact security administrator"))
                .body("messages[1].messageAction", containsString("change your password"))
                .body("messages[1].messageKey", equalTo("org.zowe.commons.zos.security.authentication.error.expired"));
    }

    @Test
    public void failsWithInternalServerError() throws Exception {
        assumeTrue("The service under test is not running on localhost",
                LOCAL_PROFILE.equalsIgnoreCase(serviceUnderTest.getProfile()));

        RestAssured.authentication = basic(VALID_USERID, FAILING_PASSWORD);
        when().get("/api/v1/securityTest/authenticatedUser").then().statusCode(500);
    }

    @Test
    public void allowsRequestToPermittedResource() throws Exception {
        when().get("/api/v1/securityTest/safProtectedResource").then().statusCode(200);
    }

    @Test
    public void forbidsRequestToDeniedResource() throws Exception {
        when().get("/api/v1/securityTest/safDeniedResource").then().statusCode(403);
    }

}
