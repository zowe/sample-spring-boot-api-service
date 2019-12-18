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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.zowe.commons.error.CommonsErrorService;
import org.zowe.commons.error.ErrorService;
import org.zowe.commons.rest.response.ApiMessage;
import org.zowe.commons.rest.response.BasicApiMessage;
import org.zowe.commons.rest.response.Message;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationProvider;
import org.zowe.commons.zos.security.platform.PlatformErrorType;
import org.zowe.commons.zos.security.platform.PlatformPwdErrno;
import org.zowe.commons.zos.security.platform.PlatformReturned;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public final class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final String UNAUTHORIZED_MESSAGE_KEY = "org.zowe.commons.rest.unauthorized";
    private static final String EXPIRED_MESSAGE_KEY = "org.zowe.commons.zos.security.authentication.error.expired";
    private static final String INTERNAL_AUTHENTICATION_ERROR_MESSAGE_KEY = "org.zowe.commons.zos.security.authentication.error.internal";

    private final ErrorService errorService = CommonsErrorService.get();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${apiml.service.title:service}")
    private String serviceTitle;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;

        ApiMessage message = errorService.createApiMessage(LocaleContextHolder.getLocale(), UNAUTHORIZED_MESSAGE_KEY, authException.getMessage());

        PlatformReturned returned = (PlatformReturned) request
                .getAttribute(ZosAuthenticationProvider.ZOWE_AUTHENTICATE_RETURNED);
        if (returned != null) {
            PlatformPwdErrno errno = PlatformPwdErrno.valueOfErrno(returned.getErrno());
            if ((errno != null) && (errno.errorType == PlatformErrorType.INTERNAL)) {
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                message = errorService.createApiMessage(LocaleContextHolder.getLocale(), INTERNAL_AUTHENTICATION_ERROR_MESSAGE_KEY, errno.explanation);
                log.error(message.toLogMessage()
                        + String.format(" Security error details: %s %s %s", errno.shortErrorName, errno.explanation, returned));
            } else if ((errno != null) && (errno.errorType == PlatformErrorType.USER_EXPLAINED)) {
                message = errorService.createApiMessage(LocaleContextHolder.getLocale(), UNAUTHORIZED_MESSAGE_KEY, errno.explanation);
                ApiMessage expiredMessage = errorService.createApiMessage(LocaleContextHolder.getLocale(), EXPIRED_MESSAGE_KEY);
                List<Message> messages = new ArrayList<>();
                messages.add(message.getMessages().get(0));
                messages.add(expiredMessage.getMessages().get(0));
                message = new BasicApiMessage(messages);
            } else {
                message = errorService.createApiMessage(LocaleContextHolder.getLocale(), UNAUTHORIZED_MESSAGE_KEY, "Incorrect credentials");
            }
        }

        response.setStatus(httpStatus.value());
        response.setContentType(MediaType.APPLICATION_JSON.toString());
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE,
                String.format("Basic realm=\"%s\", charset=\"UTF-8\"", serviceTitle));
        response.getOutputStream().println(objectMapper.writeValueAsString(message));
    }
}
