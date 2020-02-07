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

import org.springframework.http.ResponseEntity;
import org.zowe.commons.spring.login.LoginRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public interface TokenService {

    ResponseEntity login(LoginRequest loginRequest, HttpServletRequest request) throws ServletException, IOException;
    //TODO: query api
    //TODO: validateToken
}
