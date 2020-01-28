/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;


/**
 * Configuration class for authentication-related security properties
 */
//TODO: Whether we want to put them in properties file
@Data
@Component
@ConfigurationProperties(prefix = "apiml.security.auth", ignoreUnknownFields = false)
public class AuthConfigurationProperties {

    public String basicAuthenticationPrefix = "Basic";
    private String serviceLoginEndpoint = "/api/v1/auth/login";

    private AuthConfigurationProperties.TokenProperties tokenProperties;
    private AuthConfigurationProperties.CookieProperties cookieProperties;

    //Token properties
    @Data
    public static class TokenProperties {
        private int expirationTime = 24 * 60 * 60;
        private String issuer = "ZOWESDK";
        private String shortTtlUsername = "expire";
        private long shortTtlExpirationInSeconds = 1;
        //TODO: What should be the right value for it
        public String secretKeyToGenJWTs = "SecretKeyToGenJWTs";
        public String requestHeader = "Authorization";
        public String tokenPrefix = "Bearer_";
    }

    //Cookie properties
    @Data
    public static class CookieProperties {
        private String cookieName = "zoweSdkAuthenticationToken";
        private boolean cookieSecure = true;
        private String cookiePath = "/";
        private String cookieComment = "Zowe SDK security token";
        private Integer cookieMaxAge = -1;
    }

    public AuthConfigurationProperties() {
        this.cookieProperties = new AuthConfigurationProperties.CookieProperties();
        this.tokenProperties = new AuthConfigurationProperties.TokenProperties();
    }

}
