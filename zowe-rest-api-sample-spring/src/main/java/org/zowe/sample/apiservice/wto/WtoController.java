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

import static org.zowe.commons.apidoc.ApiDocConstants.DOC_SCHEME_BASIC_AUTH;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zowe.commons.zos.security.thread.PlatformThreadLevelSecurity;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;

/**
 * Handles the /wto endpoint and calls either the z/OS or off-z/OS
 * implementation of Wto depending on the spring profile settings.
 */
@Api(tags = "WTO")
@RestController
@RequestMapping("/api/v1/wto")
public class WtoController {

    private final Wto wto;

    @Autowired
    public WtoController(Wto wto, PlatformThreadLevelSecurity platformThreadLevelSecurity) {
        this.wto = wto;
    }

    private static final String TEMPLATE = "Hello, %s!";
    private final AtomicInteger counter = new AtomicInteger();

    @ApiOperation(value = "Executes WTO on z/OS and returns a greeting for the name passed", nickname = "greetingToSomeone", authorizations = {
            @Authorization(value = DOC_SCHEME_BASIC_AUTH) })
    @GetMapping
    public WtoResponse greeting(
            @ApiParam(value = "Person or object to be greeted", required = false) @RequestParam(value = "name", defaultValue = "world") String name) {
        return wto.call(counter.incrementAndGet(), String.format(TEMPLATE, name));
    }
}
