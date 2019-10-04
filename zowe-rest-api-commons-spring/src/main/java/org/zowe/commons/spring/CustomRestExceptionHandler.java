/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.spring;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.zowe.commons.error.CommonsErrorService;
import org.zowe.commons.error.ErrorService;
import org.zowe.commons.rest.response.ApiMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * Custom error message handling for REST API
 */
@ControllerAdvice
@Order
@Slf4j
public class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {
    private static final String INTERNAL_SERVER_ERROR_MESSAGE_KEY = "org.zowe.commons.rest.internalServerError";
    private static final String FORBIDDEN_MESSAGE_KEY = "org.zowe.commons.rest.forbidden";

    private final ErrorService errorService = CommonsErrorService.get();

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        ApiMessage message = errorService.createApiMessage("org.zowe.commons.rest.methodNotAllowed");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(message);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        ApiMessage message = errorService.createApiMessage("org.zowe.commons.rest.notFound");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON_UTF8).body(message);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        ApiMessage message = errorService.createApiMessage("org.zowe.commons.rest.unsupportedMediaType");
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(message);
    }

    @ExceptionHandler({ AccessDeniedException.class })
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        ApiMessage message = errorService.createApiMessage(FORBIDDEN_MESSAGE_KEY, ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON_UTF8).body(message);
    }

    @ExceptionHandler({ Exception.class })
    public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
        ApiMessage message = errorService.createApiMessage(INTERNAL_SERVER_ERROR_MESSAGE_KEY);
        log.error(message.toLogMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(message);
    }
}
