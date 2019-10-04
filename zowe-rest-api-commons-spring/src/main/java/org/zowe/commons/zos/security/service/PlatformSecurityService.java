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

import org.zowe.commons.zos.security.platform.PlatformAccessControl.AccessLevel;

/**
 * Provides low-level security function that are z/OS platform dependent.
 *
 * Known implementations:
 * {@link org.zowe.commons.zos.security.service.DummyPlatformSecurityService},
 * {@link org.zowe.commons.zos.security.service.ZosJniPlatformSecurityService}
 */
public interface PlatformSecurityService {
    /**
     * @return The current user ID of the security context of the current thread.
     */
    String getCurrentThreadUserId();

    /**
     * Create a thread-level security context (ACEE). ApplID can be null if you want
     * to use the default one (OMVSAPPL).
     */
    void createThreadSecurityContext(String userId, String password, String applId);

    /**
     * Create a thread-level security context (ACEE) without password using the
     * daemon authority.
     */
    void createThreadSecurityContextByDaemon(String userId, String applId);

    /**
     * Remove the current thread-level security context (ACEE).
     */
    void removeThreadSecurityContext();

    /**
     * Check if a specific user has permission to a resource.
     */
    boolean checkPermission(String userid, String resourceClass, String resourceName, AccessLevel accessLevel, boolean resourceHasToExist);

    /**
     * Check if a specific user has permission to an existing resource.
     */
    boolean checkPermission(String userid, String resourceClass, String resourceName, AccessLevel accessLevel);

    /**
     * Check current ID permission to a resource.
     */
    boolean checkPermission(String resourceClass, String resourceName, AccessLevel accessLevel, boolean resourceHasToExist);

    /**
     * Check current ID permission to an existing resource.
     */
    boolean checkPermission(String resourceClass, String resourceName, AccessLevel accessLevel);
}
