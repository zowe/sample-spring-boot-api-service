package org.zowe.commons.spring.token;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public interface TokenService {

    TokenResponse login(LoginRequest loginRequest, HttpServletRequest request) throws ServletException, IOException;
    //TODO: query api
    //TODO: validateToken
}
