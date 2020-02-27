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
import lombok.EqualsAndHashCode;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

@Data
@EqualsAndHashCode(callSuper = false)
public class TokenAuthentication extends AbstractAuthenticationToken {
    private final String principal;
    private final String credentials;

    public TokenAuthentication(String token) {
        super(Collections.emptyList());
        this.principal = null;
        this.credentials = token;
    }
}

