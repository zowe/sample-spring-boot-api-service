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

import java.util.HashSet;
import java.util.Set;

import org.zowe.commons.zos.security.platform.PlatformAccessControl;
import org.zowe.commons.zos.security.platform.PlatformAccessControl.AccessLevel;
import org.zowe.commons.zos.security.platform.PlatformAckErrno;
import org.zowe.commons.zos.security.platform.PlatformErrno2;
import org.zowe.commons.zos.security.platform.PlatformReturned;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AccessControlService implements PlatformSecurityService {
    private Set<String> validatedServerSecurity = new HashSet<>();

    protected PlatformAccessControl platformAccessControl = null;

    protected PlatformAccessControl getPlatformAccessControl() {
        return platformAccessControl;
    }

    @Override
    public boolean checkPermission(String userid, String resourceClass, String resourceName, AccessLevel accessLevel,
            boolean resourceHasToExist) {
        validateServerSecurity("check permission", "FACILITY", "BPX.SERVER", AccessLevel.READ);
        PlatformReturned returned = getPlatformAccessControl().checkPermission(userid, resourceClass, resourceName,
                accessLevel.getValue());
        return evaluatePlatformReturned(returned, resourceHasToExist);
    }

    private boolean evaluatePlatformReturned(PlatformReturned returned, boolean resourceHasToExist) {
        if (returned == null) {
            return true;
        } else {
            String message;
            PlatformAckErrno errno = PlatformAckErrno.valueOfErrno(returned.errno);
            PlatformErrno2 errno2 = PlatformErrno2.valueOfErrno(returned.errno2);
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
                log.error("Platform access control failed: {} {} {} {}", errno.name, errno2.name, errno2.explanation,
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

    protected void validateServerSecurity(String action, String resourceClass, String resourceName,
            AccessLevel accessLevel) {

        String resourceString = String.format("%s.%s.%s", resourceClass, resourceName, accessLevel.toString());
        if (!validatedServerSecurity.contains(resourceString)) {
            boolean result = checkPermission(resourceClass, resourceName, accessLevel);
            if (!result) {
                String message = String.format("%s access to resource %s in %s class is required for the %s action",
                        accessLevel.toString(), resourceName, resourceClass, action);
                log.error(message);
            } else {
                validatedServerSecurity.add(resourceString);
            }
        }
    }
}
