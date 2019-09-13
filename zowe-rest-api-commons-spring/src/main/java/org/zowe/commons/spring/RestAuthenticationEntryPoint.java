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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.zowe.commons.error.CommonsErrorService;
import org.zowe.commons.error.ErrorService;
import org.zowe.commons.rest.response.ApiMessage;

@Component
public final class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ErrorService errorService = CommonsErrorService.get();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${apiml.service.title:service}")
    private String serviceTitle;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        ApiMessage message = errorService.createApiMessage("org.zowe.commons.rest.unauthorized",
                authException.getMessage());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON.toString());
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, String.format("Basic realm=\"%s\", charset=\"UTF-8\"", serviceTitle));
        response.getOutputStream().println(objectMapper.writeValueAsString(message));
    }
}
