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

public class MockPlatformAccessControl implements PlatformAccessControl {
    public static final String VALID_USERID = "zowe";
    public static final String VALID_USERID2 = "zowe2";
    public static final String PERMITTED_RESOURCE = "PERMITTED";
    public static final String BPX_SERVER = "BPX.SERVER";
    public static final String DENIED_RESOURCE = "DENIED";
    public static final String UNDEFINED_RESOURCE = "UNDEFINED";
    public static final String FAILING_RESOURCE = "FAILING";
	public static final String VALID_CLASS = "FACILITY";

    @Override
    public PlatformReturned checkPermission(String userid, String resourceClass, String resourceName,
            int accessLevel) {
        boolean validUserid = userid.equalsIgnoreCase(VALID_USERID) || userid.equalsIgnoreCase(VALID_USERID2);
        if (validUserid && (resourceName.equalsIgnoreCase(PERMITTED_RESOURCE)
                || (resourceClass.equalsIgnoreCase("FACILITY") && resourceName.equalsIgnoreCase("BPX.SERVER")))) {
            return null;
        } else {
            PlatformReturned.PlatformReturnedBuilder builder = PlatformReturned.builder().success(false);
            if (!validUserid) {
                builder.errno(PlatformAckErrno.ESRCH.errno).errno2(PlatformErrno2.JRSAFNoUser.errno2);
            } else if (resourceName.equalsIgnoreCase(DENIED_RESOURCE)) {
                builder.errno(PlatformAckErrno.EPERM.errno).errno2(PlatformErrno2.JRNoResourceAccess.errno2);
            } else if (resourceName.equalsIgnoreCase(UNDEFINED_RESOURCE)) {
                builder.errno(PlatformAckErrno.ESRCH.errno).errno2(PlatformErrno2.JRSAFResourceUndefined.errno2);
            } else if (resourceName.equalsIgnoreCase(FAILING_RESOURCE)) {
                builder.errno(PlatformAckErrno.EPERM.errno).errno2(PlatformErrno2.JREnvDirty.errno2);
            }
            return builder.build();
        }
    }

    @Override
    public PlatformReturned checkPermission(String resourceClass, String resourceName, int accessLevel) {
        return checkPermission(VALID_USERID, resourceClass, resourceName, accessLevel);
    }
}
