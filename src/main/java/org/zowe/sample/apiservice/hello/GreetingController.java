/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.hello;

import static org.zowe.sample.apiservice.apidoc.ApiDocConstants.DOC_SCHEME_BASIC_AUTH;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zowe.sample.apiservice.config.RestApiVersion1Controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;

@Api(tags = "Greeting", description = "REST API for greetings")
@RestApiVersion1Controller
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @ApiOperation(value = "Returns a greeting for the name passed", nickname = "greetingToSomeone", authorizations = {
            @Authorization(value = DOC_SCHEME_BASIC_AUTH) })
    @GetMapping("/greeting")
    public Greeting greeting(
            @ApiParam(value = "Person or object to be greeted", required = false) @RequestParam(value = "name", defaultValue = "world") String name) {
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }
}
