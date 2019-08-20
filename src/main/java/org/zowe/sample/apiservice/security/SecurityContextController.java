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

import static org.zowe.sample.apiservice.apidoc.ApiDocConstants.DOC_SCHEME_BASIC_AUTH;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zowe.sdk.zos.security.PlatformSecurityService;
import org.zowe.sdk.zos.security.PlatformThreadLevelSecurity;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import springfox.documentation.annotations.ApiIgnore;

@Api(tags = "Security", description = "REST API to test security functions in SDK")
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

    @ApiOperation(value = "Changes security context on the platform during the call and returns infromation about user IDs", authorizations = {
            @Authorization(value = DOC_SCHEME_BASIC_AUTH) })
    @GetMapping("/authenticatedUser")
    public Map<String, String> authenticated(@ApiIgnore Authentication authentication) {
        Map<String, String> result = new LinkedHashMap<>();
        String beforeSwitchUserName = platformSecurityService.getCurrentThreadUserId();
        result.put("authenticatedUserName", authentication.getName());
        result.put("beforeSwitchUserName", beforeSwitchUserName);
        platformThreadLevelSecurity.wrapRunnableInEnvironmentForAuthenticatedUser(new Runnable() {
            @Override
            public void run() {
                String afterSwitchUserName = platformSecurityService.getCurrentThreadUserId();
                String afterSwitchUserNameSpring = SecurityContextHolder.getContext().getAuthentication().getName();
                result.put("afterSwitchUserName", afterSwitchUserName);
                result.put("afterSwitchUserNameSpring", afterSwitchUserNameSpring);
            }
        }).run();

        String afterRemoveUserName = platformSecurityService.getCurrentThreadUserId();
        result.put("afterRemoveUserName", afterRemoveUserName);
        return result;
    }
}
