package org.zowe.commons.spring.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Filter to validate JWT token in all the rest API's.
 */
public class JWTAuthorizationFilter extends AbstractTokenHandler {

    private final AuthConfigurationProperties authConfigurationProperties;

    public JWTAuthorizationFilter(TokenFailureHandler failureHandler,
                                  AuthConfigurationProperties authConfigurationProperties) {
        super(failureHandler, authConfigurationProperties);
        this.authConfigurationProperties = authConfigurationProperties;
    }

    @Override
    protected Optional<AbstractAuthenticationToken> extractContent(HttpServletRequest request) {
        return Optional.of(
            new TokenAuthentication(request.getHeader(
                authConfigurationProperties.getTokenProperties().getRequestHeader())));
    }
}
