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

public enum PlatformErrorType {
    /**
     * Error during the security processing which means that security product did
     * not allow the action but no details should be shared with the user - e.g.
     * invalid user ID or password
     */
    DEFAULT,
    /**
     * Error during the security processing which means that security product did
     * not allow the action and details should be shared with the user - e.g expired
     * password
     */
    USER_EXPLAINED,
    /**
     * Internal failure during the security processing which means that security
     * product or the service are not configured properly
     */
    INTERNAL,
    /**
     * errno2 value is required to understand the cause of the failure
     */
    ERRNO2_REQUIRED
}
