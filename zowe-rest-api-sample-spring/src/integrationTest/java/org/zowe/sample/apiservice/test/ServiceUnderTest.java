/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionTimeoutException;
import org.zowe.commons.spring.config.ZoweAuthenticationUtility;

import java.util.Base64;
import java.util.Map;

import static io.restassured.RestAssured.basic;
import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.awaitility.Awaitility.await;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.VALID_PASSWORD;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.VALID_USERID;

@Slf4j
@Getter
@ToString
public class ServiceUnderTest {
    public static final String LOCAL_PROFILE = "local";
    public static final String ZOS_PROFILE = "zos";

    enum Status {
        UNKNOWN, UP, DOWN
    }

    private static ServiceUnderTest instance;

    private ZoweAuthenticationUtility authConfigurationProperties
        = new ZoweAuthenticationUtility();

    private final String profile;

    private final String baseUri;

    private final int port;
    private final String userId;

    @ToString.Exclude
    private final String password;

    private final String healthEndpoint;

    private final String cookieName;

    private final String loginEndpoint;

    private final int waitMinutes;

    private Status status = Status.UNKNOWN;

    public synchronized static ServiceUnderTest getInstance() {
        if (instance == null) {
            instance = new ServiceUnderTest();
        }
        return instance;
    }

    public ServiceUnderTest() {
        this.profile = env("TEST_PROFILE", LOCAL_PROFILE);
        this.baseUri = env("TEST_BASE_URI", "https://localhost");
        this.port = Integer.parseInt(env("TEST_PORT", "10080"));
        this.userId = env("TEST_USERID", VALID_USERID);
        this.password = env("TEST_PASSWORD", VALID_PASSWORD);
        this.healthEndpoint = env("TEST_HEALTH_ENDPOINT", "/actuator/health");
        this.loginEndpoint = env("TEST_LOGIN_ENDPOINT", "/api/v1/auth/login");
        this.cookieName = env("TEST_COOKIE_NAME", "zoweSdkAuthenticationToken");
        this.waitMinutes = Integer.parseInt(env("TEST_WAIT_MINUTES", "1"));
        log.info("Service under test: {}", this.toString());
    }

    public String env(String name, String defaultValue) {
        Map<String, String> env = System.getenv();
        if (env.containsKey(name)) {
            return env.get(name);
        } else {
            return defaultValue;
        }
    }

    public void defaultRestAssuredSetup() {
        RestAssured.baseURI = baseUri;
        RestAssured.port = port;
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.authentication = basic(userId, password);
    }

    public boolean isReady() {
        String token = login();
        try {
            //TODO: Remove token as header as 'actuator/health' should not be blocked by spring security
            return given().header("Authorization", authConfigurationProperties.BEARER_AUTHENTICATION_PREFIX + token).when()
                .get(healthEndpoint).body().jsonPath().get("status").equals("UP");
        } catch (Exception e) {
            log.debug("Check has failed", e);
            return false;
        }
    }

    public String login() {
        String zoweBasicAuthHeader = ZoweAuthenticationUtility.BASIC_AUTHENTICATION_PREFIX
            + Base64.getEncoder().encodeToString((userId + ":" + password).getBytes());
        try {
            return given().header(authConfigurationProperties.getAuthorizationHeader(), zoweBasicAuthHeader).
                contentType(ContentType.JSON).
                post(loginEndpoint).cookie(cookieName);
        } catch (Exception e) {
            log.debug("Check has failed", e);
            return null;
        }
    }

    public synchronized void waitUntilIsReady() {
        switch (getStatus()) {
            case UNKNOWN:
                log.info("Waiting for the service {} on port {} to start", baseUri, port);
                defaultRestAssuredSetup();
                try {
                    await().atMost(waitMinutes, MINUTES).until(this::isReady);
                    status = Status.UP;
                } catch (ConditionTimeoutException e) {
                    status = Status.DOWN;
                    throw e;
                }
                break;
            case UP:
                defaultRestAssuredSetup();
                break;
            case DOWN:
                throw new RuntimeException("The service is down");
        }
    }
}
