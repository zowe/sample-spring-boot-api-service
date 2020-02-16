/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.spring.query;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zowe.commons.error.TokenExpireException;
import org.zowe.commons.error.TokenNotValidException;
import org.zowe.commons.spring.config.ZoweAuthenticationUtility;
import org.zowe.commons.spring.token.QueryResponse;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Service
public class QueryService {


    @Autowired
    ZoweAuthenticationUtility zoweAuthenticationUtility;

    public void setZoweAuthenticationUtility(ZoweAuthenticationUtility zoweAuthenticationUtility) {
        this.zoweAuthenticationUtility = zoweAuthenticationUtility;
    }

    /**
     * Check the validity of the token that is gained from the request object
     *
     * @param request - the HttpServletRequest from the client
     * @return String - extracts the token from the HttpServletRequest
     *
     */
    public String extractToken(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        Optional<String> optionalCookie = Arrays.stream(cookies)
            .filter(cookie -> cookie.getName().equals(zoweAuthenticationUtility.getCookieTokenName()))
            .filter(cookie -> !cookie.getValue().isEmpty())
            .findFirst()
            .map(Cookie::getValue);

        return optionalCookie.orElseGet(() -> request.getHeader(zoweAuthenticationUtility.getAuthorizationHeader()));
    }

    /**
     * Generate a QueryResponse object from the claims that are extracted from the token
     *
     * @param request the HttpServletRequest
     * @return returns a Query response object that contains the user, token issuing time, and token expiration time
     *
     */
    public QueryResponse query(HttpServletRequest request) {
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
     * @param jwtToken the token either form the authentication header or the cookie
     * @return extracts the claims from the token and returns it
     *
     */
    public Claims getClaims(String jwtToken) {
        try {
            jwtToken = jwtToken.replaceFirst(zoweAuthenticationUtility.getBearerAuthenticationPrefix(), "").trim();
            return Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(zoweAuthenticationUtility.getSecretKey()))
                .parseClaimsJws(jwtToken).getBody();
        } catch (ExpiredJwtException e) {
            log.debug("Token with id '{}' for user '{}' is expired.", e.getClaims().getId(), e.getClaims().getSubject());
            throw new TokenExpireException("Token is Expired.");
        } catch (JwtException e) {
            log.debug("Token is not valid due to: {}.", e.getMessage());
            throw new TokenNotValidException("Token is not valid.");
        } catch (Exception e) {
            log.debug("Token is not valid due to: {}.", e.getMessage());
            throw new TokenNotValidException("An internal error occurred while validating the token therefore the token is no longer valid.");
        }
    }

}
