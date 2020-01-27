package org.zowe.sample.apiservice.security;


import com.auth0.jwt.JWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationProvider;
import org.zowe.sample.apiservice.config.AuthConfigurationProperties;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static java.util.Collections.emptyList;
import static org.zowe.sample.apiservice.config.AuthConfigurationProperties.*;

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

    public String successfulAuthentication(UserDetails user) throws IOException, ServletException {

        return JWT.create()
            .withSubject(user.getUsername())
            .withExpiresAt(new Date(System.currentTimeMillis() + authConfigurationProperties.getTokenProperties().getExpirationInSeconds()))
            .sign(HMAC512(authConfigurationProperties.getTokenProperties().getSecretKeyToGenJWTs().getBytes()));
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

    public static String decode(String encodedString) {
        byte[] decoded = Base64.getDecoder().decode(encodedString.getBytes(StandardCharsets.UTF_8));
        return new String(decoded, StandardCharsets.UTF_8);
    }
}
