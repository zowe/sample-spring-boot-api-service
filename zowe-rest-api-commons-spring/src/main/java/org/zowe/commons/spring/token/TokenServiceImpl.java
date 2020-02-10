/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.spring.token;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.zowe.commons.spring.config.ZoweAuthenticationUtility;
import org.zowe.commons.spring.login.LoginRequest;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationProvider;

import javax.servlet.http.HttpServletRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenServiceImpl extends ZosAuthenticationProvider implements TokenService {
    private final ZoweAuthenticationUtility authConfigurationProperties;

    /**
     * Calls authentication manager to validate the username and password
     *
     * @return the authenticated token
     */
    @Override
    public ResponseEntity login(LoginRequest loginRequest,
                                HttpServletRequest request) {
        if (loginRequest == null) {
            loginRequest = authConfigurationProperties.getCredentialFromAuthorizationHeader(request).get();
        }

        UsernamePasswordAuthenticationToken authentication
            = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

        authenticate(authentication);
        return ResponseEntity.ok(new TokenResponse(authConfigurationProperties.createToken(authentication)));
    }
}
