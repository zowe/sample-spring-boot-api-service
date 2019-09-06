/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.greeting;

import static org.zowe.sample.apiservice.apidoc.ApiDocConstants.DOC_SCHEME_BASIC_AUTH;

import java.util.concurrent.atomic.AtomicLong;

import com.ca.mfaas.rest.response.ApiMessage;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;

@Api(tags = "Greeting", description = "REST API for greetings")
@RestController
@RequestMapping("/api/v1/greeting")
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping
    @ApiOperation(value = "Returns a greeting for the name passed", nickname = "greetingToSomeone", authorizations = {
            @Authorization(value = DOC_SCHEME_BASIC_AUTH) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful greeting", response = Greeting.class),
            @ApiResponse(code = 400, message = "Invalid request parameter - empty name", response = ApiMessage.class) })
    public Greeting greeting(
            @ApiParam(value = "Person or object to be greeted", required = false) @RequestParam(value = "name", defaultValue = "world") String name) {
        if (name.trim().isEmpty()) {
            throw new EmptyNameError();
        }
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }

    @ApiOperation(value = "This greeting always fails and provides example how unhandled exceptions are reported", nickname = "failedGreeting", authorizations = {
            @Authorization(value = DOC_SCHEME_BASIC_AUTH) })
    @GetMapping("/failed")
    public Greeting failedGreeting() {
        throw new RuntimeException("exception");
    }
}
