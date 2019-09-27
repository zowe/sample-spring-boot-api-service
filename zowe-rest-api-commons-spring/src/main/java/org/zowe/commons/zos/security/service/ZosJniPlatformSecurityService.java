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

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.zowe.commons.zos.security.jni.Secur;
import org.zowe.commons.zos.security.platform.PlatformThread;
import org.zowe.commons.zos.security.platform.PlatformTlsErrno;
import org.zowe.commons.zos.security.platform.SafPlatformThread;

import lombok.extern.slf4j.Slf4j;

import static org.zowe.commons.zos.CommonsNativeLibraries.SECUR_LIBRARY_NAME;

/**
 * Implements low-level security functions using pthread_security_np() that is
 * called via JNI in module libsecur.so.
 */
@Profile("zos")
@Service("platformSecurityService")
@Slf4j
public class ZosJniPlatformSecurityService implements PlatformSecurityService {
    private static final int CREATE_THREAD_SECURITY_CONTEXT = 0;
    private static final int REMOVE_THREAD_SECURITY_CONTEXT = 1;

    private final Secur secur = new Secur();
    private final PlatformThread safPlatformThread = new SafPlatformThread();

    @Override
    public void createThreadSecurityContext(String userId, String password, String applId) {
        checkErrno("create thread-level security environment",
                secur.createSecurityEnvironment(userId, password, applId), CREATE_THREAD_SECURITY_CONTEXT);
    }

    private void checkErrno(String action, int errno, int function) {
        if (errno != 0) {
            PlatformTlsErrno tlsErrno = PlatformTlsErrno.valueOfErrno(errno);
            String explanation;
            if (tlsErrno != null) {
                explanation = tlsErrno.name + " " + tlsErrno.explanation;
            } else {
                explanation = "unknown reason";
            }
            log.error("Platform security action to {} has failed: {}; errno={}", action, explanation, errno);
            throw new SecurityRequestFailed(SECUR_LIBRARY_NAME, function, errno);
        }
    }

    @Override
    public void createThreadSecurityContextByDaemon(String userId, String applId) {
        checkErrno("create thread-level security environment without password",
                secur.createSecurityEnvironmentByDaemon(userId, applId), CREATE_THREAD_SECURITY_CONTEXT);
    }

    @Override
    public String getCurrentThreadUserId() {
        return safPlatformThread.getUserName();
    }

    @Override
    public void removeThreadSecurityContext() {
        checkErrno("remove thread-level security environment", secur.removeSecurityEnvironment(),
                REMOVE_THREAD_SECURITY_CONTEXT);
    }
}
