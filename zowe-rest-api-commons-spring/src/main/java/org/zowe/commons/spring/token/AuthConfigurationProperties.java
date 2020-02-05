package org.zowe.commons.spring.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;


/**
 * Configuration class for authentication-related security properties
 */
//TODO: Whether we want to put them in properties file
@Data
@Component
@Slf4j
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
        public String secretKeyToGenJWTs = "8Zz5tw0Ionm3XPZZfN0NOml3z9FMfmpgXwovR9fp6ryDIoGRM8EPHAB6iHsc0fb";
        public String requestHeader = "Authorization";
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

    /**
     * Decode the encoded credentials
     *
     * @param base64Credentials the credentials encoded in base64
     * @return the decoded credentials in {@link LoginRequest}
     */
    public LoginRequest mapBase64Credentials(String base64Credentials) {
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        int i = credentials.indexOf(':');
        if (i > 0) {
            return new LoginRequest(credentials.substring(0, i), credentials.substring(i + 1));
        }

        return null;
    }

    /**
     * Get credentials from the request body
     *
     * @param request the http request
     * @return the credentials in {@link LoginRequest}
     * @throws AuthenticationCredentialsNotFoundException if the login object has wrong format
     */
    public LoginRequest getCredentialsFromBody(HttpServletRequest request) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(request.getInputStream(), LoginRequest.class);
        } catch (IOException e) {
            log.debug("Authentication problem: login object has wrong format");
            throw new AuthenticationCredentialsNotFoundException("Login object has wrong format.");
        }
    }

    /**
     * Extract credentials from the authorization header in the request and decode them
     *
     * @param request the http request
     * @return the decoded credentials
     */
    public Optional<LoginRequest> getCredentialFromAuthorizationHeader(HttpServletRequest request) {
        return Optional.ofNullable(
            request.getHeader(HttpHeaders.AUTHORIZATION)
        ).filter(
            header -> header.startsWith(getBasicAuthenticationPrefix())
        ).map(
            header -> header.replaceFirst(getBasicAuthenticationPrefix(), "").trim()
        )
            .filter(base64Credentials -> !base64Credentials.isEmpty())
            .map(this::mapBase64Credentials);
    }
}
