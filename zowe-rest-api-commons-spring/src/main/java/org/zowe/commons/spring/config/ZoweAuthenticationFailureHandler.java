/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.spring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.zowe.commons.error.CommonsErrorService;
import org.zowe.commons.error.ErrorService;
import org.zowe.commons.rest.response.ApiMessage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class ZoweAuthenticationFailureHandler {

    private final ErrorService errorService = CommonsErrorService.get();

    private static final String CONTENT_TYPE = MediaType.APPLICATION_JSON_UTF8_VALUE;

    protected final ObjectMapper mapper;

    private ApiMessage localizedMessage(String key) {
        return errorService.createApiMessage(LocaleContextHolder.getLocale(), key);
    }

    /**
     * Entry method that takes care of an exception passed to it
     *
     * @param ex Exception to be handled
     * @throws ServletException Fallback exception if exception cannot be handled
     */
    public void handleException(RuntimeException ex,
                                HttpServletResponse httpServletResponse) throws ServletException {
        if (ex instanceof SignatureException) {
            handleInvalidTokenException(httpServletResponse);
        } else if (ex instanceof ExpiredJwtException) {
            handleExpiredTokenException(httpServletResponse);
        } else if (ex instanceof MalformedJwtException) {
            handleInvalidTokenException(httpServletResponse);
        } else if (ex instanceof InsufficientAuthenticationException) {
            handleUnauthorizedException(ex, httpServletResponse);
        } else if (ex instanceof BadCredentialsException) {
            handleUnauthorizedException(ex, httpServletResponse);
        } else if (ex instanceof AuthenticationCredentialsNotFoundException) {
            handleUnauthorizedException(ex, httpServletResponse);
        } else if (ex instanceof NullPointerException) {
            handleUnauthorizedException(ex, httpServletResponse);
        } else if (ex instanceof ResourceAccessException) {
            handleUnauthorizedException(ex, httpServletResponse);
        } else {
            throw ex;
        }
    }

    private void handleInvalidTokenException(HttpServletResponse response) throws ServletException {
        ApiMessage message = localizedMessage("org.zowe.commons.rest.invalidToken");
        writeErrorResponse(message, HttpStatus.EXPECTATION_FAILED, response);
    }

    private void handleUnauthorizedException(Exception exception, HttpServletResponse response) throws ServletException {
        ApiMessage message = errorService.createApiMessage(LocaleContextHolder.getLocale(),
            "org.zowe.commons.rest.unauthorized",
            exception.getMessage());
        writeErrorResponse(message, HttpStatus.UNAUTHORIZED, response);
    }

    private void handleExpiredTokenException(HttpServletResponse response) throws ServletException {
        ApiMessage message = localizedMessage("org.zowe.commons.rest.expiredToken");
        writeErrorResponse(message, HttpStatus.NOT_ACCEPTABLE, response);
    }

    private void handleResourceAccessException(HttpServletResponse response) throws ServletException {
        ApiMessage message = localizedMessage("org.zowe.commons.rest.forbidden");
        writeErrorResponse(message, HttpStatus.FORBIDDEN, response);
    }

    protected void writeErrorResponse(ApiMessage message, HttpStatus status, HttpServletResponse response) throws ServletException {
        response.setStatus(status.value());
        response.setContentType(CONTENT_TYPE);
        try {
            mapper.writeValue(response.getWriter(), message);
        } catch (IOException e) {
            throw new ServletException("Error writing response", e);
        }
    }
}
