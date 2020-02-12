package org.zowe.commons.spring.query.exceptions;

import org.springframework.security.core.AuthenticationException;

/**
 * This exception is thrown in case the JWT token is not valid.
 */
public class TokenNotValidException extends AuthenticationException {

    public TokenNotValidException(String msg) {
        super(msg);
    }

    public TokenNotValidException(String msg, Throwable t) {
        super(msg, t);
    }
}
