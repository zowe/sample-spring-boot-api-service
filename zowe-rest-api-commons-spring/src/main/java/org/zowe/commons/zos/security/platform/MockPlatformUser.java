/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.zos.security.platform;

public class MockPlatformUser implements PlatformUser {
    public static final String VALID_USERID = "zowe";
    public static final String VALID_USERID2 = "zowe2";
    public static final String VALID_PASSWORD = "zowe";
    public static final String INVALID_USERID = "notzowe";
    public static final String INVALID_PASSWORD = "notzowe";
    public static final String EXPIRED_PASSWORD = "expired";
    public static final String FAILING_PASSWORD = "failing";

    @Override
    public PlatformReturned authenticate(String userid, String password) {
        if ((userid.equalsIgnoreCase(VALID_USERID) || userid.equalsIgnoreCase(VALID_USERID2))
                && password.equalsIgnoreCase(VALID_PASSWORD)) {
            return null;
        } else {
            PlatformReturned.PlatformReturnedBuilder builder = PlatformReturned.builder().success(false);
            if (EXPIRED_PASSWORD.equalsIgnoreCase(password)) {
                builder.errno(PlatformPwdErrno.EMVSEXPIRE.errno);
            } else if (FAILING_PASSWORD.equalsIgnoreCase(password)) {
                builder.errno(PlatformPwdErrno.EPERM.errno);
            }
            return builder.build();
        }
    }
}
