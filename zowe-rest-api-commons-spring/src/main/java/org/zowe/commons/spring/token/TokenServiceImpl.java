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

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.zowe.commons.spring.config.ZoweAuthenticationFailureHandler;
import org.zowe.commons.spring.config.ZoweAuthenticationUtility;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationProvider;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;

@Service
@Slf4j
@Data
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    @Autowired
    ZoweAuthenticationUtility zoweAuthenticationUtility;

    @Autowired
    ZoweAuthenticationFailureHandler zoweAuthenticationFailureHandler;

    @Autowired
    ZosAuthenticationProvider zosAuthenticationProvider;

    /**
     * Calls authentication manager to validate the username and password
     *
     * @return the authenticated token
     */
    @Override
    public String login(LoginRequest loginRequest,
                        HttpServletRequest request,
                        HttpServletResponse response) throws ServletException {
        String token = null;
        try {
            loginRequest = validateRequestAndExtractLoginRequest(loginRequest, request);
            UsernamePasswordAuthenticationToken authentication
                = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

            zosAuthenticationProvider.authenticate(authentication);

            token = zoweAuthenticationUtility.createToken(authentication);
            zoweAuthenticationUtility.setCookie(token, response);
        } catch (RuntimeException exception) {
            zoweAuthenticationFailureHandler.handleException(exception, response);
        }
        return token;
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
        Optional<String> authHeader = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION));
        if ((authHeader.filter(
            header -> header.startsWith(ZoweAuthenticationUtility.basicAuthenticationPrefix)))
            .isPresent()) {
            loginRequest = zoweAuthenticationUtility.getCredentialFromAuthorizationHeader(request).orElse(new LoginRequest());
        } else if (loginRequest.getUsername().isEmpty() || loginRequest.getPassword().isEmpty()) {
            throw new AuthenticationCredentialsNotFoundException("Credentials Not found");
        }
        return loginRequest;
    }

    /**
     * Check the validity of the token that is gained from the request object
     *
     * @param request - the HttpServletRequest from the client
     * @return String - extracts the token from the HttpServletRequest
     */
    private String extractToken(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        String cookieName = zoweAuthenticationUtility.getCookieTokenName();
        Optional<String> optionalCookie = Arrays.stream(cookies)
            .filter(cookie -> cookie.getName().equals(cookieName))
            .filter(cookie -> !cookie.getValue().isEmpty())
            .findFirst()
            .map(Cookie::getValue);

        return optionalCookie.orElseGet(() -> request.getHeader(zoweAuthenticationUtility.getAuthorizationHeader()));
    }

    /**
     * Generate a QueryResponse object from the claims that are extracted from the token
     *
     * @param request the HttpServletRequest
     * @return returns a Query response object that contains the user, token issuing time, and token expiration time
     */
    public QueryResponse query(HttpServletRequest request) {
        String jwtToken = extractToken(request);
        if (!StringUtils.isEmpty(jwtToken)) {
            return zoweAuthenticationUtility.getClaims(jwtToken);
        }
        return null;
    }

    @Override
    public boolean validateToken(String jwtToken) {
        if (Optional.ofNullable(zoweAuthenticationUtility.getClaims(jwtToken)).isPresent())
            return true;
        else
            return false;
    }

}


