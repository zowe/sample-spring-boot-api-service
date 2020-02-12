package org.zowe.commons.spring.query;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zowe.commons.spring.config.ZoweAuthenticationUtility;
import org.zowe.commons.spring.query.exceptions.TokenExpireException;
import org.zowe.commons.spring.query.exceptions.TokenNotValidException;
import org.zowe.commons.spring.token.QueryResponse;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Optional;

@Slf4j
@Service
public class QueryService {


    @Autowired
    ZoweAuthenticationUtility zoweAuthenticationUtility;

    /**
     * Check the validity of the token that is gained from the request object
     *
     * @param request - the HttpServletRequest from the client
     * @return boolean - that tells whether the token is valid or not
     * @throws Exception -throws an exception if the claims can't be retrieved from the token
     */
    public String extractToken(HttpServletRequest request) throws Exception {

        Cookie[] cookies = request.getCookies();
        Optional<String> optionalCookie = Arrays.stream(cookies)
            .filter(cookie -> cookie.getName().equals(zoweAuthenticationUtility.getCookieProperties().getCookieName()))
            .filter(cookie -> !cookie.getValue().isEmpty())
            .findFirst()
            .map(Cookie::getValue);

        if (optionalCookie.isPresent()) {
            return String.valueOf(optionalCookie);
        } else {
            return request.getHeader(zoweAuthenticationUtility.getTokenProperties().getRequestHeader());
        }
    }

    /**
     * Generate a QueryResponse object from the claims that are extracted from the token
     *
     * @param request
     * @return
     * @throws Exception
     */
    public QueryResponse query(HttpServletRequest request) throws Exception {
        String jwtToken = extractToken(request);
        if (!jwtToken.equals("")) {
            Claims claims = getClaims(jwtToken);
            return new QueryResponse(claims.getSubject(), claims.getIssuedAt(), claims.getExpiration());
        }
        return null;
    }

    /**
     * Get the Claims from the Token String in the authentication header/cookie
     *
     * @param jwtToken
     * @return
     */
    public Claims getClaims(String jwtToken) {
        try {
            return Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(zoweAuthenticationUtility.getTokenProperties().getSecretKeyToGenJWTs()))
                .parseClaimsJws(jwtToken).getBody();
        } catch (ExpiredJwtException e) {
            log.debug("Token with id '{}' for user '{}' is expired.", e.getClaims().getId(), e.getClaims().getSubject());
            throw new TokenExpireException("Token is Expired.");
        } catch (JwtException e) {
            log.debug("Token is not valid due to: {}.", e.getMessage());
            throw new TokenNotValidException("Token is not valid.");
        } catch (Exception e) {
            log.debug("Token is not valid due to: {}.", e.getMessage());
            throw new TokenNotValidException("An internal error occurred while validating the token therefor the token is no longer valid.");
        }
    }

    /**
     * Https isn't working with the standard methods for some reason
     * this is a method that we're using for testing
     * @param token
     * @return
     */
    public QueryResponse queryHttps(String token) {
        Claims claims = null;
        try {
            claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(zoweAuthenticationUtility.getTokenProperties().getSecretKeyToGenJWTs()))
                .parseClaimsJws(token).getBody();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (claims != null) {
            return new QueryResponse(claims.getSubject(), claims.getIssuedAt(), claims.getExpiration());
        }
        return new QueryResponse("Invalid", Calendar.getInstance().getTime(), Calendar.getInstance().getTime());
    }

}
