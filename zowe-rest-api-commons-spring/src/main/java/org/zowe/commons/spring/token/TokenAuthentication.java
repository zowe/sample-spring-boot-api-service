package org.zowe.commons.spring.token;

import lombok.EqualsAndHashCode;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

@EqualsAndHashCode(callSuper = false)
public class TokenAuthentication extends AbstractAuthenticationToken {
    private final String username;
    private final String token;

    public TokenAuthentication(String token) {
        super(Collections.emptyList());
        this.username = null;
        this.token = token;
    }

    /**
     * @return the token that prove the username is correct
     */
    @Override
    public String getCredentials() {
        return token;
    }

    /**
     * @return the username being authenticated
     */
    @Override
    public String getPrincipal() {
        return username;
    }
}
