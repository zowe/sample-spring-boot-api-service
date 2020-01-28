package org.zowe.sample.apiservice.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationProvider;
import org.zowe.sample.apiservice.config.AuthConfigurationProperties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Custom class to authenticate the user and to create and set JWT token as cookie response header
 */
@Slf4j
@Component
public class SampleApiAuthenticationProvider extends ZosAuthenticationProvider {

    private final AuthConfigurationProperties authConfigurationProperties;

    public SampleApiAuthenticationProvider(AuthConfigurationProperties authConfigurationProperties) {
        this.authConfigurationProperties = authConfigurationProperties;
    }

    /**
     * This method will be used to create JWT token after successful SAF authentication.
     *
     * @param user
     * @return
     */
    public String onSuccessfulLoginCreateToken(LoginRequest user) {
        long now = System.currentTimeMillis();

        return authConfigurationProperties.getTokenProperties().getTokenPrefix() + Jwts.builder()
            .setSubject(user.getUsername())
            .setExpiration(new Date(now + authConfigurationProperties.getTokenProperties().getExpirationTime() * 1000))
            .signWith(SignatureAlgorithm.HS512, authConfigurationProperties.getTokenProperties().getSecretKeyToGenJWTs())
            .setIssuedAt(new Date(now))
            .compact();
    }

    /**
     * Method to set the cookie in the response. Which will contain the JWT token,HTTP flag etc.
     *
     * @param token
     * @param response
     */
    public void setCookie(String token, HttpServletResponse response) {
        Cookie tokenCookie = new Cookie(authConfigurationProperties.getCookieProperties().getCookieName(),
            token);
        tokenCookie.setComment(authConfigurationProperties.getCookieProperties().getCookieComment());
        tokenCookie.setPath(authConfigurationProperties.getCookieProperties().getCookiePath());
        tokenCookie.setHttpOnly(true);
        tokenCookie.setMaxAge(authConfigurationProperties.getCookieProperties().getCookieMaxAge());
        tokenCookie.setSecure(authConfigurationProperties.getCookieProperties().isCookieSecure());

        response.addCookie(tokenCookie);
    }

    /**
     * Parse the JWT token and return a {@link QueryResponse} object containing the domain, user id, date of creation and date of expiration
     *
     * @param token the JWT token
     * @return the query response
     */
    public QueryResponse parseJwtToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(authConfigurationProperties.getTokenProperties().getSecretKeyToGenJWTs())
            .parseClaimsJws(token.replace(authConfigurationProperties.getTokenProperties().getTokenPrefix(), ""))
            .getBody();

        return new QueryResponse(
            claims.getSubject(),
            claims.getIssuedAt(),
            claims.getExpiration());
    }
}
