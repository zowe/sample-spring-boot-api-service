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

import io.restassured.module.mockmvc.kotlin.extensions.Given
import io.restassured.module.mockmvc.kotlin.extensions.Then
import io.restassured.module.mockmvc.kotlin.extensions.When
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.MessageSource
import org.springframework.http.HttpHeaders

@WebMvcTest
internal class GreetingControllerTest {

    @Autowired
    lateinit var messageSource: MessageSource

    @Test
    fun `when greeting endpoint is called, Hello world is returned`() {
        Given {
            standaloneSetup(GreetingController(messageSource))
        } When {
            get("/api/v1/greeting")
        } Then {
            statusCode(200)
            body("content", equalTo("Hello, world!"))
        }
    }

    @Test
    fun `when greeting endpoint is called with parameter name, Hello Jirka is returned`() {
        Given {
            standaloneSetup(GreetingController(messageSource))
            param("name","Jirka")
        } When {
            get("/api/v1/greeting")
        } Then {
            statusCode(200)
            body("content", equalTo("Hello, Jirka!"))
        }
    }

    @Test
    fun `when greeting endpoint is called with parameter name and czech language, Ahoj Jirka is returned`() {
        Given {
            standaloneSetup(GreetingController(messageSource))
            header(HttpHeaders.ACCEPT_LANGUAGE, "cs")
            param("name","Jirka")
        } When {
            get("/api/v1/greeting")
        } Then {
            statusCode(200)
            body("content", equalTo("Ahoj, Jirka!"))
        }
    }

}
