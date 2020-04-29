/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.kotlin.apiservice.test

import io.restassured.RestAssured
import io.restassured.RestAssured.get
import mu.KotlinLogging
import org.awaitility.core.ConditionTimeoutException
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import java.time.Duration

internal class ServiceUnderTest {
    private val log = KotlinLogging.logger {}

    internal enum class Status {
        UNKNOWN, UP, DOWN
    }

    private var status = Status.UNKNOWN

    private val baseUri = System.getenv("TEST_BASE_URI") ?: "https://localhost"
    private val port = System.getenv("TEST_PORT") ?: "10090"
    private val healthEndpoint = System.getenv("TEST_HEALTH_ENDPOINT") ?: "/actuator/health"
    private val waitMinutes = System.getenv("TEST_WAIT_MINUTES") ?: "1"

    fun waitUntilIsReady() {
        when (status) {
            Status.UNKNOWN -> {
                log.info { "Waiting for the service $baseUri on port $port to start" }
                defaultRestAssuredSetup()
                status = try {
                    await atMost Duration.ofMinutes(waitMinutes.toLong()) until { isReady() }
                    Status.UP
                } catch (e: ConditionTimeoutException) {
                    log.error { "Service $baseUri on port $port is down. Timeout after $waitMinutes minute(s)." }
                    Status.DOWN
                    throw RuntimeException("The service is down")
                }
            }
            Status.UP -> defaultRestAssuredSetup()
            Status.DOWN -> {
                log.error { "Service $baseUri on port $port is down." }
                throw RuntimeException("The service is down")
            }
        }
    }

    fun isReady(): Boolean {
        return try {
            get(healthEndpoint).body().jsonPath().get<String>("status") == "UP"
        } catch (e: Exception) {
            log.debug("Check has failed", e)
            false
        }
    }

    private fun defaultRestAssuredSetup() {
        RestAssured.baseURI = baseUri
        RestAssured.port = port.toInt()
        RestAssured.useRelaxedHTTPSValidation()
    }
}
