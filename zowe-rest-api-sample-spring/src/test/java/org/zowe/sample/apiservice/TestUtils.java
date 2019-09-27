/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice;

import static org.zowe.commons.zos.security.platform.MockPlatformUser.EXPIRED_PASSWORD;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.FAILING_PASSWORD;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.INVALID_PASSWORD;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.VALID_PASSWORD;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.VALID_USERID;

import java.util.Base64;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.zowe.commons.zos.security.platform.MockPlatformUser;

public class TestUtils {
    public static final String ZOWE_BASIC_AUTHENTICATION = "Basic "
            + Base64.getEncoder().encodeToString((VALID_USERID + ":" + VALID_PASSWORD).getBytes());
    public static final String ZOWE_BASIC_AUTHENTICATION_INVALID = "Basic "
            + Base64.getEncoder().encodeToString((VALID_USERID + ":" + INVALID_PASSWORD).getBytes());
    public static final String ZOWE_BASIC_AUTHENTICATION_FAILING = "Basic "
            + Base64.getEncoder().encodeToString((VALID_USERID + ":" + FAILING_PASSWORD).getBytes());
    public static final String ZOWE_BASIC_AUTHENTICATION_EXPIRED = "Basic "
            + Base64.getEncoder().encodeToString((VALID_USERID + ":" + EXPIRED_PASSWORD).getBytes());
    public static final UsernamePasswordAuthenticationToken ZOWE_AUTHENTICATION_TOKEN = new UsernamePasswordAuthenticationToken(
            MockPlatformUser.VALID_USERID, null);
}
