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

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.zowe.commons.zos.security.platform.MockPlatformUser;

public class TestUtils {
    public static final String ZOWE_BASIC_AUTHENTICATION = "Basic em93ZTp6b3dl";
    public static final UsernamePasswordAuthenticationToken ZOWE_AUTHENTICATION_TOKEN = new UsernamePasswordAuthenticationToken(MockPlatformUser.VALID_USERID, null);
}
