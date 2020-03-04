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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.EXPIRED_PASSWORD;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.FAILING_PASSWORD;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.INVALID_PASSWORD;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.INVALID_USERID;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.VALID_PASSWORD;
import static org.zowe.commons.zos.security.platform.MockPlatformUser.VALID_USERID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public class ZosAuthenticationProviderTests {
    private static ZosAuthenticationProvider provider = new ZosAuthenticationProvider();
    private UsernamePasswordAuthenticationToken VALID_TOKEN = new UsernamePasswordAuthenticationToken(VALID_USERID,
            VALID_PASSWORD);
    private UsernamePasswordAuthenticationToken INVALID_TOKEN = new UsernamePasswordAuthenticationToken(INVALID_USERID,
            INVALID_PASSWORD);
    private UsernamePasswordAuthenticationToken EXPIRED_TOKEN = new UsernamePasswordAuthenticationToken(INVALID_USERID,
            EXPIRED_PASSWORD);
    private UsernamePasswordAuthenticationToken FAILING_TOKEN = new UsernamePasswordAuthenticationToken(INVALID_USERID,
            FAILING_PASSWORD);

    @BeforeClass
    public static void setup() throws Exception {
        provider.afterPropertiesSet();
    }

    @Test(expected = ZosAuthenticationException.class)
    public void exceptionOnInvalidCredentials() {
        provider.authenticate(INVALID_TOKEN);
    }

    @Test(expected = ZosAuthenticationException.class)
    public void exceptionOnExpiredCredentials() {
        provider.authenticate(EXPIRED_TOKEN);
    }

    @Test(expected = ZosAuthenticationException.class)
    public void exceptionOnFailingCredentials() {
        provider.authenticate(FAILING_TOKEN);
    }

    @Test
    public void validAuthenticationOnValidCredentials() {
        Authentication authentication = provider.authenticate(VALID_TOKEN);
        assertEquals(VALID_USERID, authentication.getPrincipal());
    }

    @Test
    public void supportsUsernamePasswordAuthenticationToken() {
        assertTrue(provider.supports(VALID_TOKEN.getClass()));
    }

}
