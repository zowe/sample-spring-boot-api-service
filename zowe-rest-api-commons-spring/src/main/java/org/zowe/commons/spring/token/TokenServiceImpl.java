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
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.zowe.commons.spring.config.ZoweAuthenticationFailureHandler;
import org.zowe.commons.spring.config.ZoweAuthenticationUtility;
import org.zowe.commons.spring.login.LoginRequest;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final ZoweAuthenticationUtility authConfigurationProperties;
    private final ZoweAuthenticationFailureHandler zoweAuthenticationFailureHandler;

    @Autowired
    ZosAuthenticationProvider zosAuthenticationProvider;

    /**
     * Calls authentication manager to validate the username and password
     *
     * @return the authenticated token
     */
    @Override
    public ResponseEntity login(LoginRequest loginRequest,
                                HttpServletRequest request,
                                HttpServletResponse response) throws ServletException {
        try {
            loginRequest = validateRequestAndExtractLoginRequest(loginRequest, request);

            UsernamePasswordAuthenticationToken authentication
                = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

            zosAuthenticationProvider.authenticate(authentication);

            String token = authConfigurationProperties.createToken(authentication);
            authConfigurationProperties.setCookie(token, response);
        } catch (RuntimeException exception) {
            zoweAuthenticationFailureHandler.handleException(exception, response);
        }
        return ResponseEntity
            .status(HttpStatus.SC_OK)
            .body(new AppResponse("OK", HttpStatus.SC_OK, "User is authenticated"));
    }

    /**
     * This method will check if the credentials are present in body or header.
     * If it is null in both of them then it will throw an unauthorized error
     *
     * @param loginRequest
     * @param request
     * @return
     * @throws ServletException
     */
    private LoginRequest validateRequestAndExtractLoginRequest(LoginRequest loginRequest,
                                                               HttpServletRequest request) throws ServletException {
        if ((Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION)).filter(
            header -> header.startsWith(authConfigurationProperties.getBasicAuthenticationPrefix())))
            .isPresent()) {
            loginRequest = authConfigurationProperties.getCredentialFromAuthorizationHeader(request).get();
        } else if (loginRequest.getUsername().isEmpty() || loginRequest.getPassword().isEmpty()) {
            throw new AuthenticationCredentialsNotFoundException("Credentials Not found");
        }

        return loginRequest;
    }
}
