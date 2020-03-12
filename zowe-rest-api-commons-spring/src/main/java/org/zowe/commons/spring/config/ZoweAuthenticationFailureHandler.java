/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.spring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
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
import org.zowe.commons.zos.security.authentication.ZosAuthenticationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class ZoweAuthenticationFailureHandler {

    private static final String CONTENT_TYPE = MediaType.APPLICATION_JSON_UTF8_VALUE;

    @Value("${apiml.service.title:service}")
    private String serviceTitle;

    protected ApiMessage localizedMessage(String key) {
        ErrorService errorService = CommonsErrorService.get();
        return errorService.createApiMessage(LocaleContextHolder.getLocale(), key);
    }

    protected ApiMessage localizedMessage(String key, String message) {
        ErrorService errorService = CommonsErrorService.get();
        return errorService.createApiMessage(LocaleContextHolder.getLocale(), key, message);
    }

    /**
     * Entry method that takes care of an exception passed to it
     *
     * @param ex Exception to be handled
     * @throws ServletException Fallback exception if exception cannot be handled
     */
    public boolean handleException(RuntimeException ex,
                                   HttpServletResponse httpServletResponse) throws ServletException {
        if (ex instanceof UnsupportedJwtException) {
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
            handleResourceAccessException(httpServletResponse);
        } else if (ex instanceof ZosAuthenticationException) {
            handleUnauthorizedException(ex, httpServletResponse);
        } else {
            throw new InsufficientAuthenticationException("Authentication failed");
        }
        return false;
    }

    private void handleInvalidTokenException(HttpServletResponse response) throws ServletException {
        ApiMessage message = localizedMessage("org.zowe.commons.rest.invalidToken");
        writeErrorResponse(message, HttpStatus.UNAUTHORIZED, response);
    }

    private void handleUnauthorizedException(Exception exception, HttpServletResponse response) throws ServletException {
        ErrorService errorService = CommonsErrorService.get();
        ApiMessage message = errorService.createApiMessage(LocaleContextHolder.getLocale(),
            "org.zowe.commons.rest.unauthorized", exception.getMessage());
        writeErrorResponse(message, HttpStatus.UNAUTHORIZED, response);
    }

    private void handleExpiredTokenException(HttpServletResponse response) throws ServletException {
        ApiMessage message = localizedMessage("org.zowe.commons.rest.expiredToken");
        writeErrorResponse(message, HttpStatus.UNAUTHORIZED, response);
    }

    //org.zowe.commons.rest.forbidden has a %s string in the text .. so it needs an additional argument in the method call
    private void handleResourceAccessException(HttpServletResponse response) throws ServletException {
        ApiMessage message = localizedMessage("org.zowe.commons.rest.forbidden", "");
        writeErrorResponse(message, HttpStatus.FORBIDDEN, response);
    }

    private void writeErrorResponse(ApiMessage message, HttpStatus status, HttpServletResponse response) throws ServletException {
        ObjectMapper mapper = new ObjectMapper();
        response.setStatus(status.value());
        response.setContentType(CONTENT_TYPE);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE,
            String.format("Basic realm=\"%s\", charset=\"UTF-8\"", serviceTitle));
        try {
            mapper.writeValue(response.getWriter(), message);
        } catch (IOException e) {
            throw new ServletException("Error writing response", e);
        }
    }
}
