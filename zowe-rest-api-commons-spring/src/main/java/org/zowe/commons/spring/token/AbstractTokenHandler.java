/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.spring.token;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.zowe.commons.spring.config.ZoweAuthenticationFailureHandler;
import org.zowe.commons.spring.config.ZoweAuthenticationUtility;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationProvider;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class AbstractTokenHandler extends OncePerRequestFilter {

    private final ZoweAuthenticationFailureHandler failureHandler;
    private final ZoweAuthenticationUtility authConfigurationProperties;
    private final ZosAuthenticationProvider authenticationManager;

    /**
     * Extracts the token from the request
     *
     * @param request containing credentials
     * @return credentials
     */
    protected abstract Optional<AbstractAuthenticationToken> extractContent(HttpServletRequest request);

    @Override
    public boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().equalsIgnoreCase(authConfigurationProperties.getServiceLoginEndpoint());
    }

    /**
     * Extracts the token from the request and use the authentication manager to perform authentication.
     * Then set the currently authenticated principal and call the next filter in the chain.
     *
     * @param request     the http request
     * @param response    the http response
     * @param filterChain the filter chain
     * @throws ServletException a general exception
     * @throws IOException      a IO exception
     */
    @Override
    public void doFilterInternal(@NonNull HttpServletRequest request,
                                 @NonNull HttpServletResponse response,
                                 @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getServletPath().isEmpty() ? request.getRequestURI() : request.getServletPath();
        if (listOfAllowedEndpoints(header)) {
            filterChain.doFilter(request, response);
        } else {
            Optional<AbstractAuthenticationToken> authenticationToken = extractContent(request);
            if (authenticationToken.isPresent()) {
                try {
                    Optional<UsernamePasswordAuthenticationToken> authentication = getAuthentication(request, response);
                    if (authentication.isPresent()) {
                        SecurityContextHolder.getContext().setAuthentication(authentication.get());
                        filterChain.doFilter(request, response);
                    } else {
                        throw new InsufficientAuthenticationException("Authentication failed");
                    }
                } catch (RuntimeException authenticationException) {
                    failureHandler.handleException(authenticationException, response);
                }
            } else {
                filterChain.doFilter(request, response);
            }
        }
    }

    /**
     * List of endpoints which are public(swagger,csrf and login)
     *
     * @param header
     * @return
     */
    private boolean listOfAllowedEndpoints(String header) {
        return header.equalsIgnoreCase(authConfigurationProperties.getServiceLoginEndpoint()) ||
            header.equalsIgnoreCase("/swagger-ui.html") || header.startsWith("/webjars/") ||
            header.equalsIgnoreCase("/login") || header.startsWith("/swagger-resources") ||
            header.startsWith("/apiDocs") || header.startsWith("/favicon") || header.equalsIgnoreCase("/")
            || header.startsWith("/csrf");
    }

    public Optional<UsernamePasswordAuthenticationToken> getAuthentication(HttpServletRequest request,
                                                                           HttpServletResponse httpServletResponse) throws ServletException, IOException {
        String header = null;
        String username = null;

        Optional<UsernamePasswordAuthenticationToken> usernamePasswordAuthenticationToken = Optional.empty();

        Cookie[] cookies = request.getCookies();
        if (null != request.getHeader(authConfigurationProperties.getAuthorizationHeader())) {
            header = request.getHeader(authConfigurationProperties.getAuthorizationHeader());
        } else if (null != cookies) {
            Optional<String> optionalCookie = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(authConfigurationProperties.getCookieTokenName()))
                .filter(cookie -> !cookie.getValue().isEmpty())
                .findFirst()
                .map(Cookie::getValue);

            header = optionalCookie.orElseGet(() -> request.getHeader(authConfigurationProperties.getAuthorizationHeader()));
        }

        if (header != null) {
            if (header.startsWith(ZoweAuthenticationUtility.BEARER_AUTHENTICATION_PREFIX)) {
                header = header.replaceFirst(ZoweAuthenticationUtility.BEARER_AUTHENTICATION_PREFIX, "").trim();

                username = authConfigurationProperties.getClaims(header).getUserId();
                if (username != null) {
                    usernamePasswordAuthenticationToken = Optional.of(new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>()));
                }

            } else if (header.startsWith(ZoweAuthenticationUtility.BASIC_AUTHENTICATION_PREFIX)) {
                LoginRequest loginRequest = authConfigurationProperties.getCredentialFromAuthorizationHeader(request).orElse(new LoginRequest());
                UsernamePasswordAuthenticationToken authentication
                    = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

                authenticationManager.authenticate(authentication);
                usernamePasswordAuthenticationToken = Optional.of(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), null, new ArrayList<>()));
            } else {
                //cookies
                username = authConfigurationProperties.getClaims(header).getUserId();
                if (username != null) {
                    usernamePasswordAuthenticationToken = Optional.of(new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>()));
                }
            }
            return usernamePasswordAuthenticationToken;
        }
        return Optional.empty();
    }
}
