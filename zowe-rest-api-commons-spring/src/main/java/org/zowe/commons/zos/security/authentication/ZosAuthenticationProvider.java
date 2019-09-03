
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
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.zowe.commons.zos.security.platform.MockPlatformUser;
import org.zowe.commons.zos.security.platform.PlatformReturned;
import org.zowe.commons.zos.security.platform.PlatformUser;
import org.zowe.commons.zos.security.platform.SafPlatformClassFactory;
import org.zowe.commons.zos.security.platform.SafPlatformUser;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ZosAuthenticationProvider implements AuthenticationProvider, InitializingBean {
    private PlatformUser platformUser = null;

    @Autowired
    private Environment environment;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userid = authentication.getName();
        String password = authentication.getCredentials().toString();
        PlatformReturned returned = getPlatformUser().authenticate(userid, password);

        if ((returned == null) || (returned.isSuccess())) {
            return new UsernamePasswordAuthenticationToken(userid, null, new ArrayList<>());
        } else {
            throw new ZosAuthenticationException("Authentication failed: " + returned.toString(), returned);
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
    }
}
