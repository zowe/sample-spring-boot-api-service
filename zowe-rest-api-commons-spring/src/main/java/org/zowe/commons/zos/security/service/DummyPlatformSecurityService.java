/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.zos.security.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.zowe.commons.zos.security.platform.MockPlatformAccessControl;

import lombok.extern.slf4j.Slf4j;

/**
 * Implements security functions that provide dummy behavior outside of z/OS.
 */
@Profile("!zos")
@Service("platformSecurityService")
@Slf4j
public class DummyPlatformSecurityService extends AccessControlService implements PlatformSecurityService, InitializingBean {
    static String INVALID_VALUE = "INVALID";

    private static final SecurityRequestFailed SECURITY_REQUEST_FAILED = new SecurityRequestFailed(null, 0, 1);

    private static final String SERVER_USERID = System.getProperty("user.name");

    ThreadLocal<String> threadLocalUserId = ThreadLocal.withInitial(() -> SERVER_USERID);

    @Override
    public void createThreadSecurityContext(String userId, String password, String applId) {
        if (userId.equalsIgnoreCase(INVALID_VALUE) || password.equalsIgnoreCase(INVALID_VALUE)) {
            throw SECURITY_REQUEST_FAILED;
        }
        threadLocalUserId.set(userId);
    }

    @Override
    public void createThreadSecurityContextByDaemon(String userId, String applId) {
        if (userId.equalsIgnoreCase(INVALID_VALUE)) {
            throw SECURITY_REQUEST_FAILED;
        }
        threadLocalUserId.set(userId);
    }

    @Override
    public String getCurrentThreadUserId() {
        return threadLocalUserId.get();
    }

    @Override
    public void removeThreadSecurityContext() {
        threadLocalUserId.set(SERVER_USERID);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
       platformAccessControl = new MockPlatformAccessControl();
       log.warn("The mock authorization check provider is used. This application should not be used in production");
    }
}
