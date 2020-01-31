/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.security;

import static org.zowe.commons.zos.security.platform.SafConstants.BPX_SERVER;
import static org.zowe.commons.zos.security.platform.SafConstants.CLASS_FACILITY;
import static org.zowe.commons.apidoc.ApiDocConstants.DOC_SCHEME_BASIC_AUTH;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zowe.commons.zos.security.platform.PlatformAccessControl.AccessLevel;
import org.zowe.commons.zos.security.service.PlatformSecurityService;
import org.zowe.commons.zos.security.thread.PlatformThreadLevelSecurity;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import springfox.documentation.annotations.ApiIgnore;

@Api(tags = "Security")
@RestController
@RequestMapping("/api/v1/securityTest")
public class SecurityContextController {
    private final PlatformSecurityService platformSecurityService;
    private final PlatformThreadLevelSecurity platformThreadLevelSecurity;

    @Autowired
    public SecurityContextController(PlatformSecurityService platformSecurityService,
            PlatformThreadLevelSecurity platformThreadLevelSecurity) {
        this.platformSecurityService = platformSecurityService;
        this.platformThreadLevelSecurity = platformThreadLevelSecurity;
    }

    @ApiOperation(value = "Changes security context on the platform during the call and returns information about user IDs", authorizations = {
            @Authorization(value = DOC_SCHEME_BASIC_AUTH) })
    @GetMapping("/authenticatedUser")
    public Map<String, String> authenticated(@ApiIgnore Authentication authentication) {
        Map<String, String> result = new LinkedHashMap<>();
        String beforeSwitchUserName = platformSecurityService.getCurrentThreadUserId();
        boolean accessToBpxServerServer = platformSecurityService.checkPermission(CLASS_FACILITY, BPX_SERVER,
                AccessLevel.READ);
        boolean accessToBpxServerUserid = platformSecurityService.checkPermission(authentication.getName(),
                CLASS_FACILITY, BPX_SERVER, AccessLevel.READ);
        result.put("authenticatedUserName", authentication.getName());
        result.put("beforeSwitchUserName", beforeSwitchUserName);
        result.put("accessToBpxServerServer", Boolean.toString(accessToBpxServerServer));
        result.put("accessToBpxServerUserid", Boolean.toString(accessToBpxServerUserid));

        platformThreadLevelSecurity.wrapRunnableInEnvironmentForAuthenticatedUser(() -> {
            String afterSwitchUserName = platformSecurityService.getCurrentThreadUserId();
            String afterSwitchUserNameSpring = SecurityContextHolder.getContext().getAuthentication().getName();
            boolean accessToBpxServer = platformSecurityService.checkPermission(CLASS_FACILITY, BPX_SERVER,
                    AccessLevel.READ);
            boolean accessToUndefinedResource = platformSecurityService.checkPermission(CLASS_FACILITY, "UNDEFINED",
                    AccessLevel.READ);
            boolean accessToUndefinedResourceAllowMissingResource = platformSecurityService
                    .checkPermission(CLASS_FACILITY, "UNDEFINED", AccessLevel.READ, false);
            result.put("afterSwitchUserName", afterSwitchUserName);
            result.put("afterSwitchUserNameSpring", afterSwitchUserNameSpring);
            result.put("accessToBpxServer", Boolean.toString(accessToBpxServer));
            result.put("accessToUndefinedResource", Boolean.toString(accessToUndefinedResource));
            result.put("accessToUndefinedResourceAllowMissingResource",
                    Boolean.toString(accessToUndefinedResourceAllowMissingResource));
        }).run();

        try {
            String afterSwitchUserNameCall = (String) platformThreadLevelSecurity
                    .wrapCallableInEnvironmentForAuthenticatedUser(platformSecurityService::getCurrentThreadUserId)
                    .call();
            result.put("afterSwitchUserNameCall", afterSwitchUserNameCall);
        } catch (Exception e) {
            result.put("callException", e.toString());
        }

        String afterRemoveUserName = platformSecurityService.getCurrentThreadUserId();
        result.put("afterRemoveUserName", afterRemoveUserName);
        return result;
    }

    @ApiOperation(value = "Checks if the user ID has access to a resource", authorizations = {
            @Authorization(value = DOC_SCHEME_BASIC_AUTH) })
    @GetMapping("/resourceAccess")
    public Map<String, Object> resourceAccess(@ApiIgnore Authentication authentication,
            @RequestParam(value = "resourceClass", required = true) String resourceClass,
            @RequestParam(value = "resourceName", required = true) String resourceName,
            @RequestParam(value = "accessLevel", required = true) String accessLevel,
            @RequestParam(value = "userId", required = false) String userId) {
        if (userId == null) {
            userId = authentication.getName();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        boolean hasAccess = platformSecurityService.checkPermission(userId, resourceClass, resourceName,
                AccessLevel.valueOf(accessLevel));
        result.put("hasAccess", hasAccess);
        result.put("resourceClass", resourceClass);
        result.put("resourceName", resourceName);
        result.put("accessLevel", accessLevel);
        result.put("userId", userId);
        return result;
    }

    @ApiOperation(value = "This endpoint can be accessed only by users that have READ access to `SUPERUSER.FILESYS.MOUNT` resource in the `UNIXPRIV` class", authorizations = {
            @Authorization(value = DOC_SCHEME_BASIC_AUTH) })
    @GetMapping("/safProtectedResource")
    @PreAuthorize("hasSafResourceAccess('UNIXPRIV', 'SUPERUSER.FILESYS.MOUNT', 'READ')")
    public Map<String, String> safProtectedResource(@ApiIgnore Authentication authentication) {
        Map<String, String> result = new LinkedHashMap<>();
        boolean canMount = platformSecurityService.checkPermission(authentication.getName(), "UNIXPRIV",
                "SUPERUSER.FILESYS.MOUNT", AccessLevel.READ);
        result.put("authenticatedUserName", authentication.getName());
        result.put("canMount", Boolean.toString(canMount));
        return result;
    }

    @ApiOperation(value = "This endpoint can be accessed only by users that have CONTROL access to `SAMPLE.RESOURCE` resource in the `ZOWE` class", authorizations = {
            @Authorization(value = DOC_SCHEME_BASIC_AUTH) })
    @GetMapping("/safDeniedResource")
    @PreAuthorize("hasSafServiceResourceAccess('RESOURCE', 'CONTROL')")
    public void safDeniedResource(@ApiIgnore Authentication authentication) {
        // This is never called since the nobody has access to the resource
    }
}
