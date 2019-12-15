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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zowe.commons.rest.response.ApiMessage;
import org.zowe.commons.zos.security.platform.SafPlatformError;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import springfox.documentation.annotations.ApiIgnore;

@Api(tags = "Greeting")
@RestController
@RequestMapping("/api/v1/greeting")
public class GreetingController implements MessageSourceAware {
    private MessageSource messageSource;

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private static Map<String, String> greeting = new HashMap<>();
    private final AtomicLong counter = new AtomicLong();

    @GetMapping
    @ApiOperation(value = "Returns a greeting for the name passed", nickname = "getGreeting", authorizations = {
            @Authorization(value = DOC_SCHEME_BASIC_AUTH) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful greeting", response = Greeting.class),
            @ApiResponse(code = 400, message = "Invalid request parameter - empty name", response = ApiMessage.class) })
    public Greeting getGreeting(
            @ApiParam(value = "Person or object to be greeted", required = false) @RequestParam(value = "name", required = false) String name,
            @ApiIgnore Locale locale) {
        if (name == null) {
            name = messageSource.getMessage("GreetingController.world", null, locale);
        }
        if (name.trim().isEmpty()) {
            throw new EmptyNameError();
        }
        String greetingText = GreetingController.greeting.get(locale.getLanguage());
        if (greetingText == null) {
            greetingText = messageSource.getMessage("GreetingController.greeting", null, locale);
        }
        return new Greeting(counter.incrementAndGet(), String
                .format(messageSource.getMessage("GreetingController.greetingTemplate", null, locale), greetingText, name),
                locale.toLanguageTag());
    }

    @PutMapping("settings")
    @ApiOperation(value = "Changes the default greeting word", nickname = "updateGreeting", authorizations = {
            @Authorization(value = DOC_SCHEME_BASIC_AUTH) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful settings change", response = Greeting.class),
            @ApiResponse(code = 400, message = "Invalid request parameter", response = ApiMessage.class) })
    public GreetingSettings updateGreeting(@RequestBody GreetingSettings settings) {
        GreetingController.greeting.put(LocaleContextHolder.getLocale().getLanguage(), settings.getGreeting());
        return settings;
    }

    @ApiOperation(value = "This greeting always fails and provides example how unhandled exceptions are reported", nickname = "failedGreeting", authorizations = {
            @Authorization(value = DOC_SCHEME_BASIC_AUTH) })
    @GetMapping("/failed")
    public Greeting failedGreeting() {
        throw new SafPlatformError("exception");
    }
}
