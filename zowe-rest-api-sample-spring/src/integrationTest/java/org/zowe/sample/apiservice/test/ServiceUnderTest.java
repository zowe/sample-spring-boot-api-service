/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.test;

import static io.restassured.RestAssured.basic;
import static io.restassured.RestAssured.get;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.awaitility.Awaitility.await;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.VALID_PASSWORD;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.VALID_USERID;

import java.util.Map;

import org.awaitility.core.ConditionTimeoutException;

import io.restassured.RestAssured;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@ToString
public class ServiceUnderTest {
    enum Status {
        UNKNOWN, UP, DOWN
    }

    private static ServiceUnderTest instance;

    private final String baseUri;

    private final int port;

    private final String userId;

    @ToString.Exclude
    private final String password;

    private final String healthEndpoint;

    private final int waitMinutes;

    private Status status = Status.UNKNOWN;

    public synchronized static ServiceUnderTest getInstance() {
        if (instance == null) {
            instance = new ServiceUnderTest();
        }
        return instance;
    }

    public ServiceUnderTest() {
        this.baseUri = env("TEST_BASE_URI", "https://localhost");
        this.port = Integer.parseInt(env("TEST_PORT", "10080"));
        this.userId = env("TEST_USERID", VALID_USERID);
        this.password = env("TEST_PASSWORD", VALID_PASSWORD);
        this.healthEndpoint = env("TEST_HEALTH_ENDPOINT", "/actuator/health");
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
        try {
            return get(healthEndpoint).body().jsonPath().get("status").equals("UP");
        } catch (Exception e) {
            log.debug("Check has failed", e);
            return false;
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
            break;
        case DOWN:
            throw new RuntimeException("The service is down");
        }
    }
}
