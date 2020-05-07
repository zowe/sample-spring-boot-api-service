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

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.zowe.commons.rest.response.ApiMessage
import java.util.*
import java.util.concurrent.atomic.AtomicLong

@OpenAPIDefinition(info = Info(title = "Zowe Kotlin Sample REST API",
    version = "1.0.0",
    description = "Sample Kotlin Spring Boot REST API for Zowe."))
@RestController
@RequestMapping("/api/v1/greeting")
class GreetingController @Autowired constructor(var messageSource: MessageSource) {

    private val counter = AtomicLong()

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Returns a greeting for the name passed",
        tags = ["Greeting"])
    @ApiResponses(
        ApiResponse(responseCode = "200",
            description = "Successful greeting",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = Greeting::class, type = "Greeting"))]),
        ApiResponse(responseCode = "404", description = "Not found",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = ApiMessage::class))]))
    fun getGreeting(
        @Parameter(description = "Person or object to be greeted", required = false) @RequestParam(value = "name", required = false) name: String?,
        locale: Locale): Greeting {
        return createGreeting(name, locale)
    }

    private fun createGreeting(name: String?, locale: Locale): Greeting {
        val greetingMessage = messageSource.getMessage("GreetingController.greeting", null, locale)
        val greetingName = if (name.isNullOrBlank()) {
            messageSource.getMessage("GreetingController.world", null, locale)
        } else {
            name
        }
        val greeting = messageSource.getMessage("GreetingController.greetingTemplate", arrayOf(greetingMessage, greetingName), locale)

        return Greeting(counter.incrementAndGet(), greeting, locale.toLanguageTag())
    }

}
