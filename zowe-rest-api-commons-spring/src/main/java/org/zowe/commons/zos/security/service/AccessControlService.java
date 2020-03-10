/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.zos.security.service;

import org.zowe.commons.zos.security.platform.PlatformAccessControl;
import org.zowe.commons.zos.security.platform.PlatformAccessControl.AccessLevel;
import org.zowe.commons.zos.security.platform.PlatformAckErrno;
import org.zowe.commons.zos.security.platform.PlatformErrno2;
import org.zowe.commons.zos.security.platform.PlatformReturned;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AccessControlService implements PlatformSecurityService {
    protected PlatformAccessControl platformAccessControl = null;

    protected PlatformAccessControl getPlatformAccessControl() {
        return platformAccessControl;
    }

    @Override
    public boolean checkPermission(String userid, String resourceClass, String resourceName, AccessLevel accessLevel,
            boolean resourceHasToExist) {
        PlatformReturned returned = getPlatformAccessControl().checkPermission(userid, resourceClass, resourceName,
                accessLevel.getValue());
        return evaluatePlatformReturned(returned, resourceHasToExist);
    }

    private boolean evaluatePlatformReturned(PlatformReturned returned, boolean resourceHasToExist) {
        if (returned == null) {
            return true;
        } else {
            String message;
            PlatformAckErrno errno = PlatformAckErrno.valueOfErrno(returned.getErrno());
            PlatformErrno2 errno2 = PlatformErrno2.valueOfErrno(returned.getErrno2());
            if ((errno == null) || (errno2 == null)) {
                message = "Unknown access control error";
                log.error("Platform access control failed: {}", returned);
            } else {
                message = "Platform access control failed: " + errno2.explanation;
                if (!resourceHasToExist && (errno2 == PlatformErrno2.JRSAFResourceUndefined)) {
                    return true;
                } else if (errno2 == PlatformErrno2.JRNoResourceAccess) {
                    return false;
                } else if (errno2 == PlatformErrno2.JRSAFResourceUndefined) {
                    return false;
                }
                log.error("Platform access control failed: {} {} {} {}", errno.shortErrorName, errno2.shortErrorName, errno2.explanation,
                        returned);
            }
            throw new AccessControlError(message + ": " + returned.toString(), returned);
        }
    }

    @Override
    public boolean checkPermission(String userid, String resourceClass, String resourceName, AccessLevel accessLevel) {
        return checkPermission(userid, resourceClass, resourceName, accessLevel, true);
    }

    @Override
    public boolean checkPermission(String resourceClass, String resourceName, AccessLevel accessLevel,
            boolean resourceHasToExist) {
        PlatformReturned returned = getPlatformAccessControl().checkPermission(resourceClass, resourceName,
                accessLevel.getValue());
        return evaluatePlatformReturned(returned, resourceHasToExist);
    }

    @Override
    public boolean checkPermission(String resourceClass, String resourceName, AccessLevel accessLevel) {
        return checkPermission(resourceClass, resourceName, accessLevel, true);
    }
}
