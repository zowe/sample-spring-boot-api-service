/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */

/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.zos.security.authentication;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.zowe.commons.zos.security.platform.MockPlatformUser;
import org.zowe.commons.zos.security.platform.PlatformErrorType;
import org.zowe.commons.zos.security.platform.PlatformPwdErrno;
import org.zowe.commons.zos.security.platform.PlatformReturned;
import org.zowe.commons.zos.security.platform.PlatformUser;
import org.zowe.commons.zos.security.platform.SafPlatformClassFactory;
import org.zowe.commons.zos.security.platform.SafPlatformUser;
import org.zowe.commons.zos.security.platform.SafUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ZosAuthenticationProvider implements AuthenticationProvider, InitializingBean {
    public static final String ZOWE_AUTHENTICATE_RETURNED = "zowe.authenticate.returned";

    private PlatformUser platformUser = null;

    @Autowired
    private Environment environment;

    @Value("${zowe.commons.security.saf.applid:}")
    private String applid;

    @Override
    public Authentication authenticate(Authentication authentication) {
        String userid = authentication.getName();
        String password = authentication.getCredentials().toString();
        SafUtils.setThreadApplid(applid);
        PlatformReturned returned = getPlatformUser().authenticate(userid, password);

        if ((returned == null) || (returned.isSuccess())) {
            return new UsernamePasswordAuthenticationToken(userid, null, new ArrayList<>());
        } else {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                requestAttributes.setAttribute(ZOWE_AUTHENTICATE_RETURNED, returned, RequestAttributes.SCOPE_REQUEST);
            }

            String message;
            PlatformPwdErrno errno = PlatformPwdErrno.valueOfErrno(returned.getErrno());
            if (errno == null) {
                message = "Authentication error";
                log.debug("Platform authentication failed: {}", returned);
            } else {
                log.debug("Platform authentication failed: {} {} {}", errno.shortErrorName, errno.explanation, returned);
                if (errno.errorType == PlatformErrorType.INTERNAL) {
                    message = "Internal authentication error: " + errno.explanation;
                } else if (errno.errorType == PlatformErrorType.USER_EXPLAINED) {
                    message = "Authentication error: " + errno.explanation;
                } else {
                    message = "Authentication error";
                }
            }

            throw new ZosAuthenticationException(message, returned);
        }
    }

    private PlatformUser getPlatformUser() {
        return platformUser;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (platformUser == null) {
            if ((environment != null) && Arrays.asList(environment.getActiveProfiles()).contains("zos")) {
                platformUser = new SafPlatformUser(new SafPlatformClassFactory());
            } else {
                platformUser = new MockPlatformUser();
                log.warn("The mock authentication provider is used. This application should not be used in production");
            }
        }
        if ((applid != null) && !applid.isEmpty()) {
            log.info("APPLID is " + applid);
        }
        else {
            log.info("APPLID is not set. PassTickets are not enabled");
        }
    }
}
