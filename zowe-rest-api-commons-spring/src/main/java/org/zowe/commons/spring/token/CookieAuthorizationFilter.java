package org.zowe.commons.spring.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;

public class CookieAuthorizationFilter extends AbstractTokenHandler {
    private final AuthConfigurationProperties authConfigurationProperties;

    public CookieAuthorizationFilter(TokenFailureHandler failureHandler,
                                     AuthConfigurationProperties authConfigurationProperties) {
        super(failureHandler, authConfigurationProperties);
        this.authConfigurationProperties = authConfigurationProperties;
    }

    /**
     * Extract the valid JWT token from the cookies
     *
     * @param request the http request
     * @return
     */
    public Optional<AbstractAuthenticationToken> extractContent(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
            .filter(cookie -> cookie.getName().equals(
                authConfigurationProperties.getCookieProperties().getCookieName()))
            .filter(cookie -> !cookie.getValue().isEmpty())
            .findFirst()
            .map(cookie -> new TokenAuthentication(cookie.getValue()));
    }
}
