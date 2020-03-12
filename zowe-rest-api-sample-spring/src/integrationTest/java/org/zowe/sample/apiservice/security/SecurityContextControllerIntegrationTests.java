/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.security;

import org.hamcrest.Matcher;
import org.junit.Ignore;
import org.junit.Test;
import org.zowe.sample.apiservice.test.IntegrationTests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeTrue;
import static org.zowe.sample.apiservice.test.ServiceUnderTest.LOCAL_PROFILE;

public class SecurityContextControllerIntegrationTests extends IntegrationTests {

    @Test
    public void switchesContextToAuthenticatedUserId_Token() throws Exception {
        serviceUnderTest.defaultRestAssuredSetup();
        Matcher<String> equalsToAuthenticatedUserID = equalToIgnoringCase(serviceUnderTest.getUserId());
        given().header("Authorization", token).when().get("/api/v1/securityTest/authenticatedUser").then().statusCode(200)
            .body("afterSwitchUserName", equalsToAuthenticatedUserID)
            .body("afterSwitchUserNameSpring", equalsToAuthenticatedUserID)
            .body("authenticatedUserName", equalsToAuthenticatedUserID)
            .body("accessToBpxServerServer", equalTo("true"))
            .body("accessToUndefinedResource", equalTo("false"))
            .body("accessToUndefinedResourceAllowMissingResource", equalTo("true"));

    }

    @Test
    public void switchesContextToAuthenticatedUserId_Cookie() throws Exception {
        serviceUnderTest.defaultRestAssuredSetup();
        Matcher<String> equalsToAuthenticatedUserID = equalToIgnoringCase(serviceUnderTest.getUserId());
        given().cookie(cookieName, token.split(" ")[1]).when().get("/api/v1/securityTest/authenticatedUser").then().statusCode(200)
            .body("afterSwitchUserName", equalsToAuthenticatedUserID)
            .body("afterSwitchUserNameSpring", equalsToAuthenticatedUserID)
            .body("authenticatedUserName", equalsToAuthenticatedUserID)
            .body("accessToBpxServerServer", equalTo("true"))
            .body("accessToUndefinedResource", equalTo("false"))
            .body("accessToUndefinedResourceAllowMissingResource", equalTo("true"));

    }

    @Ignore
    public void failsWithInvalidAuthentication() throws Exception {
        String invalidToken = "eyJhbGciOiJIUzUxMiJ9.fyryerytuytry.KILjk1gpVxLY1wrr8";
        given().header("Authorization", invalidToken).when().get("/api/v1/securityTest/authenticatedUser").then().statusCode(401);
    }

    //TODO: Identify how we can expire a token
    @Ignore
    public void failsWithExpiredAuthentication() throws Exception {
        assumeTrue("The service under test is not running on localhost",
            LOCAL_PROFILE.equalsIgnoreCase(serviceUnderTest.getProfile()));

        given().header("Authorization", token).when().get("/api/v1/securityTest/authenticatedUser").then().statusCode(401)
            .body("messages[0].messageContent", containsString("expired"))
            .body("messages[0].messageReason", containsString("missing valid authentication credentials"))
            .body("messages[0].messageAction", containsString("contact security administrator"))
            .body("messages[1].messageAction", containsString("change your password"))
            .body("messages[1].messageKey", equalTo("org.zowe.commons.zos.security.authentication.error.expired"));
    }

    //TODO: Take care of internal server error scenario
    @Ignore
    public void failsWithInternalServerError() throws Exception {
        assumeTrue("The service under test is not running on localhost",
            LOCAL_PROFILE.equalsIgnoreCase(serviceUnderTest.getProfile()));

        given().header("Authorization", token).when().get("/api/v1/securityTest/authenticatedUser").then().statusCode(500);
    }

    @Test
    public void allowsRequestToPermittedResource() throws Exception {
        given().header("Authorization", token).when().get("/api/v1/securityTest/safProtectedResource").then().statusCode(200);
    }

    @Test
    public void forbidsRequestToDeniedResource() throws Exception {
        given().header("Authorization", token).when().get("/api/v1/securityTest/safDeniedResource").then().statusCode(403);
    }

}
