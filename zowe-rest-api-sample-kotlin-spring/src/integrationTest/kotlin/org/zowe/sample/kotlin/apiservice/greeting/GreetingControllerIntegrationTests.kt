/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.kotlin.apiservice.greeting

import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.apache.http.HttpHeaders
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.zowe.sample.kotlin.apiservice.test.IntegrationTest
import java.util.*

const val GREETING_ENDPOINT = "/api/v1/greeting"

class GreetingControllerIntegrationTests : IntegrationTest() {

    @Test
    fun `when greeting endpoint is called in English, Hello world is returned`() {
        Given {
            header(HttpHeaders.ACCEPT_LANGUAGE, Locale.US)
        } When {
            get(GREETING_ENDPOINT)
        } Then {
            statusCode(200)
            body("content", equalTo("Hello, world!"))
        }
    }

    @Test
    fun `when greeting endpoint is called in Czech, Ahoj svete is returned`() {
        Given {
            header(HttpHeaders.ACCEPT_LANGUAGE, "cs-CZ")
        } When {
            get(GREETING_ENDPOINT)
        } Then {
            statusCode(200)
            body("content", equalTo("Ahoj, světe!"))
        }
    }

    @Test
    fun `when greeting endpoint is called with the name parameter, Hello Jirka is returned`() {
        Given {
            header(HttpHeaders.ACCEPT_LANGUAGE, Locale.US)
            param("name", "Jirka")
        } When {
            get(GREETING_ENDPOINT)
        } Then {
            statusCode(200)
            body("content", equalTo("Hello, Jirka!"))
        }
    }

}
