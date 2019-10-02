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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.zowe.commons.rest.response.ApiMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RunWith(SpringRunner.class)
public class CustomRestExceptionHandlerTests {
    @Test
    public void handleHttpRequestMethodNotSupported() {
        CustomRestExceptionHandler handler = new CustomRestExceptionHandler();
        ResponseEntity<Object> response = handler.handleHttpRequestMethodNotSupported(
                new HttpRequestMethodNotSupportedException("PUT"), null, null, null);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertTrue(response.getBody() instanceof ApiMessage);
    }

    @Test
    public void handleNoHandlerFoundException() {
        CustomRestExceptionHandler handler = new CustomRestExceptionHandler();
        ResponseEntity<Object> response = handler.handleNoHandlerFoundException(
                new NoHandlerFoundException("GET", "http://url", null), null, null, null);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof ApiMessage);
    }

    @Test
    public void handleHttpMediaTypeNotSupported() {
        CustomRestExceptionHandler handler = new CustomRestExceptionHandler();
        ResponseEntity<Object> response = handler
                .handleHttpMediaTypeNotSupported(new HttpMediaTypeNotSupportedException("message"), null, null, null);
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
        assertTrue(response.getBody() instanceof ApiMessage);
    }

    @Test
    public void handleAccessDeniedException() {
        CustomRestExceptionHandler handler = new CustomRestExceptionHandler();
        ResponseEntity<Object> response = handler.handleAccessDeniedException(new AccessDeniedException("message"),
                null);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody() instanceof ApiMessage);
    }

    @Test
    public void handleAll() {
        CustomRestExceptionHandler handler = new CustomRestExceptionHandler();
        ResponseEntity<Object> response = handler.handleAll(new Exception("message"), null);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof ApiMessage);
    }
}
