/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.wto;

import static org.zowe.sample.apiservice.apidoc.ApiDocConstants.DOC_SCHEME_BASIC_AUTH;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zowe.sample.apiservice.config.RestApiVersion1Controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;

@Api(tags = "WTO", description = "REST API for z/OS greetings via WTO")
@RestApiVersion1Controller
public class WtoController {
    
    private final Wto wto;

    @Autowired
    public WtoController(Wto wto) {
        this.wto = wto;
    }

    private static final String template = "Hello, %s!";
    private final AtomicInteger counter = new AtomicInteger();

    @ApiOperation(value = "Executes WTO on z/OS and returns a greeting for the name passed", nickname = "greetingToSomeone", authorizations = {
            @Authorization(value = DOC_SCHEME_BASIC_AUTH) })
    @GetMapping("/wto")
    public WtoDto greeting(
            @ApiParam(value = "Person or object to be greeted", required = false) @RequestParam(value = "name", defaultValue = "world") String name) {
        return wto.call(counter.incrementAndGet(), String.format(template, name));
    }
}
