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

import java.util.HashMap;
import java.util.Map;

/**
 * Provides explanation for error codes for authentication as described at
 * documentation for BPX4TLS:
 * https://www.ibm.com/support/knowledgecenter/SSLTBW_2.4.0/com.ibm.zos.v2r4.bpxb100/tls.htm
 */
public enum PlatformTlsErrno {
    EACCES("EACCES", 111, "Permission is denied; the specified password is incorrect", PlatformErrorType.DEFAULT),
    EINVAL("EINVAL", 121, "Invalid input parameters", PlatformErrorType.DEFAULT),
    EMVSERR("EMVSERR", 157, "An MVS environmental error has been detected", PlatformErrorType.INTERNAL),
    EMVSEXPIRE("EMVSEXPIRE", 168, "The password for the specified identity has expired", PlatformErrorType.USER_EXPLAINED),
    EMVSSAF2ERR("EMVSSAF2ERR", 164, "An error occurred in the security product", PlatformErrorType.INTERNAL),
    EMVSSAFEXTRERR("EMVSSAFEXTRERR", 163, "The user's access was revoked", PlatformErrorType.DEFAULT),
    ENOSYS("ENOSYS", 134, "The function is not supported on this system", PlatformErrorType.INTERNAL),
    EPERM("EPERM", 139, "The calling address space is not authorized to use this service or a load from a not program-controlled library was done in the address space", PlatformErrorType.INTERNAL),
    ESRCH("ESRCH", 143, "The identity that was specified is not defined to the security product", PlatformErrorType.DEFAULT);

    private static final Map<Integer, PlatformTlsErrno> BY_ERRNO = new HashMap<>();

    static {
        for (PlatformTlsErrno e : values()) {
            BY_ERRNO.put(e.errno, e);
        }
    }

    public final String shortErrorName;
    public final int errno;
    public final String explanation;
    public final PlatformErrorType errorType;

    private PlatformTlsErrno(String shortErrorName, int errno, String explanation, PlatformErrorType errorType) {
        this.shortErrorName = shortErrorName;
        this.errno = errno;
        this.explanation = explanation;
        this.errorType = errorType;
    }

    public static PlatformTlsErrno valueOfErrno(int errno) {
        return BY_ERRNO.getOrDefault(errno, null);
    }
}
