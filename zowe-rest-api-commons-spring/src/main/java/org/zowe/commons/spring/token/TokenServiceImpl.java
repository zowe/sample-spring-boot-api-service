package org.zowe.commons.spring.token;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationProvider;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenServiceImpl extends ZosAuthenticationProvider implements TokenService {
    private final AuthConfigurationProperties authConfigurationProperties;

    /**
     * Calls authentication manager to validate the username and password
     *
     * @return the authenticated token
     */
    @Override
    public TokenResponse login(LoginRequest loginRequest, HttpServletRequest request) {
        if (loginRequest == null) {
            loginRequest = authConfigurationProperties.getCredentialFromAuthorizationHeader(request).get();
        }

        UsernamePasswordAuthenticationToken authentication
            = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

        authenticate(authentication);
        return new TokenResponse(createToken(authentication));
    }

    /**
     * This method is used to create token with Jwts library.
     *
     * @param authentication
     * @return
     */
    private String createToken(Authentication authentication) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
            .setSubject(authentication.getName())
            .setExpiration(new Date(now + authConfigurationProperties.getTokenProperties().getExpirationTime() * 1000))
            .signWith(SignatureAlgorithm.HS512, authConfigurationProperties.getTokenProperties().getSecretKeyToGenJWTs())
            .setIssuedAt(new Date(now))
            .compact();
    }
}
