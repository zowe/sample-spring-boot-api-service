package org.zowe.sample.apiservice.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.zowe.sample.apiservice.config.AuthConfigurationProperties;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

    private final AuthConfigurationProperties authConfigurationProperties;

    public JWTAuthorizationFilter(AuthenticationManager authManager, AuthConfigurationProperties authConfigurationProperties) {
        super(authManager);
        this.authConfigurationProperties = authConfigurationProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {
        String header = req.getHeader(authConfigurationProperties.getTokenProperties().getRequestHeader());

        if (header == null || !header.startsWith(authConfigurationProperties.getTokenProperties().getTokenPrefix())) {
            chain.doFilter(req, res);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(req);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(authConfigurationProperties.getTokenProperties().getRequestHeader());
        if (token != null) {
            // parse the token.
            String user = JWT.require(Algorithm.HMAC512(authConfigurationProperties.getTokenProperties().getSecretKeyToGenJWTs().getBytes()))
                .build()
                .verify(token.replace(authConfigurationProperties.getTokenProperties().getTokenPrefix(), ""))
                .getSubject();

            if (user != null) {
                return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
            }
            return null;
        }
        return null;
    }
}
