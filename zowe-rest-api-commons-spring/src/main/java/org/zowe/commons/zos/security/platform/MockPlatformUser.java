/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.zos.security.platform;

public class MockPlatformUser implements PlatformUser {
    // Sonar exclusion: The passwords are using only for testing with this mock

    public static final String VALID_USERID = "zowe";
    public static final String VALID_USERID2 = "zowe2";
    public static final String VALID_PASSWORD = "zowe";  // NOSONAR
    public static final String INVALID_USERID = "notzowe";
    public static final String INVALID_PASSWORD = "notzowe";  // NOSONAR
    public static final String EXPIRED_PASSWORD = "expired";  // NOSONAR
    public static final String FAILING_PASSWORD = "failing";  // NOSONAR

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
                builder.errno(PlatformPwdErrno.EMVSERR.errno).errno2(PlatformErrno2.JREnvDirty.errno2);
            }
            return builder.build();
        }
    }
}
