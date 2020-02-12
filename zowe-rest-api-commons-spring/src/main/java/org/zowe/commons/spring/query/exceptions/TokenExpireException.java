package org.zowe.commons.spring.query.exceptions;

import org.springframework.security.core.AuthenticationException;

/**
 * This exception is thrown in case the JWT token is expired.
 */
public class TokenExpireException extends AuthenticationException {

    public TokenExpireException(String msg) {
        super(msg);
    }

    public TokenExpireException(String msg, Throwable t) {
        super(msg, t);
    }
}
