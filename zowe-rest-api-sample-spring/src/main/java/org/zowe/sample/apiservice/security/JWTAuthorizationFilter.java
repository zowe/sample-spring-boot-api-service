package org.zowe.sample.apiservice.security;

import io.jsonwebtoken.Jwts;
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

/**
 * Filter to validate JWT token in all the rest API's.
 */
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

    /**
     * Method to parse the token adn to validate it
     *
     * @param request
     * @return
     */
    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(authConfigurationProperties.getTokenProperties().getRequestHeader());

        if (token != null) {

            String username = Jwts.parser()
                .setSigningKey(authConfigurationProperties.getTokenProperties().getSecretKeyToGenJWTs())
                .parseClaimsJws(token.replace(authConfigurationProperties.getTokenProperties().getTokenPrefix(), ""))
                .getBody()
                .getSubject();

            if (username != null) {
                return new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
            }
            return null;
        }
        return null;
    }
}
