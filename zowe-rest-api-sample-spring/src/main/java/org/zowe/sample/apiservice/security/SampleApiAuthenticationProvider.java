package org.zowe.sample.apiservice.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationProvider;
import org.zowe.sample.apiservice.config.AuthConfigurationProperties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import static java.util.Collections.emptyList;

@Slf4j
@Component
    public class SampleApiAuthenticationProvider extends ZosAuthenticationProvider implements UserDetailsService {

    private final AuthConfigurationProperties authConfigurationProperties;

    public SampleApiAuthenticationProvider(AuthConfigurationProperties authConfigurationProperties) {
        this.authConfigurationProperties = authConfigurationProperties;
    }

    @Override
    public UserDetails loadUserByUsername(String authorizationHeader) throws UsernameNotFoundException {
        return extractAndDecodeHeader(authorizationHeader);
    }

    private User extractAndDecodeHeader(String header) {
        if (header != null && header.startsWith(authConfigurationProperties.getBasicAuthenticationPrefix())) {
            String authString = null;
            try {
                authString = decode(header.substring(6));
            } catch (IllegalArgumentException ex) {
                throw new BadCredentialsException("Failed to decode basic authentication token");
            }
            int idxDelimiter = authString.indexOf(":");
            if (idxDelimiter == -1) {
                throw new BadCredentialsException("Invalid basic authentication token");
            } else {
                return new User(authString.substring(0, idxDelimiter),
                    Base64.getEncoder().withoutPadding().encodeToString(authString.substring(idxDelimiter + 1).getBytes()),
                    emptyList());
            }
        }

        return null;
    }

    public String successfulAuthentication(UserDetails user) {
        long now = System.currentTimeMillis();

        return authConfigurationProperties.getTokenProperties().getTokenPrefix() + Jwts.builder()
            .setSubject(user.getUsername())
            .setExpiration(new Date(now + authConfigurationProperties.getTokenProperties().getExpirationInSeconds() * 1000))
            //TODO: Which algorithm should be used for signing
            .signWith(SignatureAlgorithm.HS512, authConfigurationProperties.getTokenProperties().getSecretKeyToGenJWTs())
            .setIssuedAt(new Date(now))
            .compact();
    }

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

    public void setHeader(String token, HttpServletResponse response) {
        response.setHeader(authConfigurationProperties.getTokenProperties().getRequestHeader(),
            token);
    }

    public static String decode(String encodedString) {
        byte[] decoded = Base64.getDecoder().decode(encodedString.getBytes(StandardCharsets.UTF_8));
        return new String(decoded, StandardCharsets.UTF_8);
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
