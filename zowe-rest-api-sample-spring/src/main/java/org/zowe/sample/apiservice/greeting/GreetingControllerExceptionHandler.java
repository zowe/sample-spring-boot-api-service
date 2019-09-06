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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.zowe.commons.error.ErrorService;
import org.zowe.commons.rest.response.ApiMessage;

/**
 * Creates responses for exceptional behavior of the {@link GreetingController}.
 */
@ControllerAdvice(assignableTypes = { GreetingController.class })
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GreetingControllerExceptionHandler {
    private final ErrorService errorService;

    @Autowired
    public GreetingControllerExceptionHandler(ErrorService errorService) {
        this.errorService = errorService;
    }

    @ExceptionHandler(EmptyNameError.class)
    public ResponseEntity<ApiMessage> handleEmptyName(EmptyNameError exception) {
        ApiMessage message = errorService.createApiMessage("org.zowe.sample.apiservice.greeting.empty");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON_UTF8).body(message);
    }
}
