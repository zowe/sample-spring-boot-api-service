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

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.zowe.commons.spring.config.ZoweAuthenticationFailureHandler;
import org.zowe.commons.spring.config.ZoweAuthenticationUtility;
import org.zowe.commons.spring.login.LoginRequest;

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
    private final TokenService tokenService;

    /**
     * Extracts the token from the request
     *
     * @param request containing credentials
     * @return credentials
     */
    protected abstract Optional<AbstractAuthenticationToken> extractContent(HttpServletRequest request);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (request.getRequestURI().equalsIgnoreCase(authConfigurationProperties.getServiceLoginEndpoint())) {
            return true;
        }
        return false;
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
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getServletPath().isEmpty() ? request.getRequestURI() : request.getServletPath();

        if (header.equalsIgnoreCase(authConfigurationProperties.getServiceLoginEndpoint())) {
            filterChain.doFilter(request, response);
            return;
        } else {
            Optional<AbstractAuthenticationToken> authenticationToken = extractContent(request);

            if (authenticationToken.isPresent()) {
                try {
                    UsernamePasswordAuthenticationToken authentication = getAuthentication(request, response).get();
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    filterChain.doFilter(request, response);
                } catch (AuthenticationException authenticationException) {
                    failureHandler.handleException(authenticationException, response);
                } catch (RuntimeException exception) {
                    failureHandler.handleException(exception, response);
                }
            } else {
                filterChain.doFilter(request, response);
            }
        }
    }

    private Optional<UsernamePasswordAuthenticationToken> getAuthentication(HttpServletRequest request,
                                                                            HttpServletResponse httpServletResponse) throws ServletException, IOException {
        String header = null;
        String username = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Optional<String> optionalCookie = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(authConfigurationProperties.getCookieTokenName()))
                .filter(cookie -> !cookie.getValue().isEmpty())
                .findFirst()
                .map(Cookie::getValue);

            header = optionalCookie.orElseGet(() -> request.getHeader(authConfigurationProperties.getAuthorizationHeader()));
        } else {
            header = request.getHeader(authConfigurationProperties.getAuthorizationHeader());
        }

        if (header != null) {
            if (header.startsWith(authConfigurationProperties.getBearerAuthenticationPrefix())) {
                header = header.replaceFirst(authConfigurationProperties.getBearerAuthenticationPrefix(), "").trim();

                username = Jwts.parser()
                    .setSigningKey(authConfigurationProperties.getSecretKey())
                    .parseClaimsJws(header)
                    .getBody()
                    .getSubject();
                if (username != null) {
                    return Optional.ofNullable(new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>()));
                }

            } else if (header.startsWith(authConfigurationProperties.getBasicAuthenticationPrefix())) {
                LoginRequest loginRequest = authConfigurationProperties.getCredentialFromAuthorizationHeader(request).get();
                tokenService.login(loginRequest, request, httpServletResponse);

                return Optional.ofNullable(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), null, new ArrayList<>()));
            } else { //cookies
                username = Jwts.parser()
                    .setSigningKey(authConfigurationProperties.getSecretKey())
                    .parseClaimsJws(header)
                    .getBody()
                    .getSubject();
                if (username != null) {
                    return Optional.ofNullable(new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>()));
                }
            }
            return null;
        }
        return null;
    }
}
