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

/**
 * Interface to retrieve z/OS access-control information.
 *
 * It is inspired by
 * https://www.ibm.com/support/knowledgecenter/en/SSYKE2_8.0.0/com.ibm.java.zsecurity.api.80.doc/com.ibm.os390.security/com/ibm/os390/security/PlatformAccessControl.html
 * class. But it provides interface instead of class with static methods so
 * non-z/OS implementation of this interface can be created.
 *
 * Related IBM documentation:
 *
 * Return codes (errnos) -
 * https://www.ibm.com/support/knowledgecenter/SSLTBW_2.4.0/com.ibm.zos.v2r4.bpxa800/errno.htm
 * auth_check_resource_np (BPX1ACK, BPX4ACK) — Determine a user's access to a
 * RACF-protected resource -
 * https://www.ibm.com/support/knowledgecenter/SSLTBW_2.4.0/com.ibm.zos.v2r4.bpxb100/ack.htm
 * __check_resource_auth_np() — Determine access to MVS resources -
 * https://www.ibm.com/support/knowledgecenter/en/SSLTBW_2.4.0/com.ibm.zos.v2r4.bpxbd00/rckaut.htm
 * Class PlatformAccessControl -
 * https://www.ibm.com/support/knowledgecenter/en/SSYKE2_8.0.0/com.ibm.java.zsecurity.api.80.doc/com.ibm.os390.security/com/ibm/os390/security/PlatformAccessControl.html#checkPermission-java.lang.String-java.lang.String-java.lang.String-int-
 */
public interface PlatformAccessControl {
    /**
     * SAF permissions to resources are granted to a resource along with granularity
     * specification of one of the READ/UPDATE/CONTROL/ALTER levels.
     */
    public enum AccessLevel {
        READ(1), UPDATE(2), CONTROL(3), ALTER(4);

        private final int value;

        AccessLevel(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Check if a specific user has permission to a resource. See
     * https://www.ibm.com/support/knowledgecenter/SSYKE2_8.0.0/com.ibm.java.zsecurity.api.80.doc/com.ibm.os390.security/com/ibm/os390/security/PlatformAccessControl.html?view=embed#checkPermission-java.lang.String-java.lang.String-java.lang.String-int-
     */
    PlatformReturned checkPermission(String userid, String resourceClass, String resourceName, AccessLevel accessLevel);

    /**
     * Check current ID permission to a resource. See
     * https://www.ibm.com/support/knowledgecenter/SSYKE2_8.0.0/com.ibm.java.zsecurity.api.80.doc/com.ibm.os390.security/com/ibm/os390/security/PlatformAccessControl.html?view=embed#checkPermission-java.lang.String-java.lang.String-int-
     */
    PlatformReturned checkPermission(String resourceClass, String resourceName, AccessLevel accessLevel);
}
