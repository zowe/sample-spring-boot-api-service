/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.spring.config;

import com.ca.mfaas.security.HttpsConfig;
import com.ca.mfaas.security.HttpsConfigError;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.zowe.commons.spring.token.LoginRequest;
import org.zowe.commons.spring.token.QueryResponse;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.Optional;


/**
 * Configuration class for authentication-related security properties
 */
@Data
@Component
@Slf4j
public class ZoweAuthenticationUtility {

    public static final String BASIC_AUTHENTICATION_PREFIX = "Basic ";
    public static final String BEARER_AUTHENTICATION_PREFIX = "Bearer ";
    private String serviceLoginEndpoint = "/api/v1/auth/login";
    private String authorizationHeader = "Authorization";

    @Value("${server.ssl.keyStoreType:PKCS12}")
    private String keyStoreType;

    @Value("${apiml.security.auth.jwtKeyAlias:jwtsecret}")
    private String keyAlias;

    @Value("${zowe.commons.security.token.cookieTokenName:zoweSdkAuthenticationToken}")
    private String cookieTokenName;

    @Value("${zowe.commons.security.token.expiration:86400000}")
    private int expiration;

    @Value("${server.ssl.keyStore:#{null}}")
    private String keyStore;

    @Value("${server.ssl.keyStorePassword:#{null}}")
    private String keyStorePassword;

    @Value("${server.ssl.keyPassword:#{null}}")
    private String keyPassword;

    public static final String SAFKEYRING = "safkeyring";

    @Autowired
    ZoweAuthenticationFailureHandler zoweAuthenticationFailureHandler;

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
        } else {
            throw new AuthenticationCredentialsNotFoundException("Password is not provided");
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
            header -> header.startsWith(BASIC_AUTHENTICATION_PREFIX)
        ).map(
            header -> header.replaceFirst(BASIC_AUTHENTICATION_PREFIX, "").trim()
        )
            .filter(base64Credentials -> !base64Credentials.isEmpty())
            .map(this::mapBase64Credentials);
    }

    /**
     * This method is used to create token with Jwts library.
     *
     * @param authentication
     * @return
     */
    public String createToken(Authentication authentication) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
            .setSubject(authentication.getName())
            .setExpiration(new Date(now + Integer.valueOf(expiration)))
            .signWith(SignatureAlgorithm.HS512, getJwtSecret())
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
        Cookie tokenCookie = new Cookie(cookieTokenName, token);
        tokenCookie.setComment("Zowe SDK security token");
        tokenCookie.setPath("/");
        tokenCookie.setHttpOnly(true);
        tokenCookie.setMaxAge(-1);
        tokenCookie.setSecure(true);

        response.addCookie(tokenCookie);
    }

    /**
     * Get the Claims from the Token String in the authentication header/cookie
     *
     * @param jwtToken the token either form the authentication header or the cookie
     * @return extracts the claims from the token and returns it
     */
    public QueryResponse getClaims(String jwtToken) {
        jwtToken = jwtToken.replaceFirst(ZoweAuthenticationUtility.BEARER_AUTHENTICATION_PREFIX, "").trim();
        Claims claims = Jwts.parser()
            .setSigningKey(DatatypeConverter.parseBase64Binary(getJwtSecret()))
            .parseClaimsJws(jwtToken).getBody();
        return new QueryResponse(claims.getSubject(), claims.getIssuedAt(), claims.getExpiration());
    }

    /**
     * Reads secret key from keystore or key ring, if keystore URL starts with {@value #SAFKEYRING}, and encode to Base64
     *
     * @param config - {@link HttpsConfig} with mandatory filled fields: keyStore, keyStoreType, keyStorePassword, keyPassword,
     *               and optional filled: keyAlias and trustStore
     * @return Base64 encoded secret key in {@link String}
     */
    public static String readSecret(HttpsConfig config) {
        if (config.getKeyStore() != null) {
            try {
                Key key = loadKey(config);
                if (key == null) {
                    throw new UnrecoverableKeyException(String.format(
                        "No key with private key entry could be used in the keystore. Provided key alias: %s",
                        config.getKeyAlias() == null ? "<not provided>" : config.getKeyAlias()));
                }
                return Base64.getEncoder().encodeToString(key.getEncoded());
            } catch (UnrecoverableKeyException e) {
                throw new HttpsConfigError("Error reading secret key: " + e.getMessage(), e,
                    HttpsConfigError.ErrorCode.HTTP_CLIENT_INITIALIZATION_FAILED, config);
            }
        }
        return null;
    }

    /**
     * Loads secret key from keystore or key ring, if keystore URL starts with {@value #SAFKEYRING}
     *
     * @param config - {@link HttpsConfig} with mandatory filled fields: keyStore, keyStoreType, keyStorePassword, keyPassword,
     *               and optional filled: keyAlias and trustStore
     * @return {@link PrivateKey} or {@link javax.crypto.SecretKey} from keystore or key ring
     */
    public static Key loadKey(HttpsConfig config) {
        if (config.getKeyStore() != null) {
            try {
                KeyStore ks = loadKeyStore(config);
                char[] keyPasswordInChars = config.getKeyPassword() == null ? null : config.getKeyPassword().toCharArray();
                Key key = null;
                if (config.getKeyAlias() != null) {
                    key = ks.getKey(config.getKeyAlias(), keyPasswordInChars);
                } else {
                    key = findFirstSecretKey(ks, keyPasswordInChars);
                }
                return key;
            } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException
                | UnrecoverableKeyException e) {
                throw new HttpsConfigError(e.getMessage(), e,
                    HttpsConfigError.ErrorCode.HTTP_CLIENT_INITIALIZATION_FAILED, config);
            }
        }
        return null;
    }

    private static Key findFirstSecretKey(KeyStore keyStore, char[] keyPasswordInChars) throws KeyStoreException, NoSuchAlgorithmException {
        Key key = null;
        for (Enumeration<String> e = keyStore.aliases(); e.hasMoreElements(); ) {
            String alias = e.nextElement();
            try {
                key = keyStore.getKey(alias, keyPasswordInChars);
                if (key != null) {
                    break;
                }
            } catch (UnrecoverableKeyException uke) {
                log.debug("Key with alias {} could not be used: {}", alias, uke.getMessage());
            }
        }
        return key;
    }

    public String getJwtSecret() {
        HttpsConfig config = HttpsConfig.builder().keyStore(keyStore).keyPassword(keyPassword)
            .keyStorePassword(keyStorePassword).keyStoreType(keyStoreType).build();
        return readSecret(config);
    }

    /**
     * Loads keystore or key ring, if keystore URL starts with {@value #SAFKEYRING}, from specified location
     *
     * @param config {@link HttpsConfig} with mandatory filled fields: keyStore, keyStoreType, keyStorePassword,
     *               and optional filled: trustStore
     * @return the new {@link KeyStore} or key ring as {@link KeyStore}
     * @throws KeyStoreException
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     */
    public static KeyStore loadKeyStore(HttpsConfig config) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance(config.getKeyStoreType());
        InputStream inputStream;
        if (config.getKeyStore().startsWith(SAFKEYRING)) {
            URL url = keyRingUrl(config.getKeyStore(), config.getTrustStore());
            inputStream = url.openStream();
        } else {
            File keyStoreFile = new File(config.getKeyStore());
            inputStream = new FileInputStream(keyStoreFile);
        }
        ks.load(inputStream, config.getKeyStorePassword() == null ? null : config.getKeyStorePassword().toCharArray());
        return ks;
    }

    /**
     * Creates an {@link URL} to key ring location
     *
     * @param uri        - key ring location
     * @param trustStore - truststore location
     * @return the new {@link URL} with 2 slashes instead of 4
     * @throws MalformedURLException throws in case of incorrect key ring format
     */
    public static URL keyRingUrl(String uri, String trustStore) throws MalformedURLException {
        if (!uri.startsWith(SAFKEYRING + ":////")) {
            throw new MalformedURLException("Incorrect key ring format: " + trustStore
                + ". Make sure you use format safkeyring:////userId/keyRing");
        }
        return new URL(replaceFourSlashes(uri));
    }

    /**
     * Replaces 4 slashes on 2 in URI
     *
     * @param storeUri - URI as {@link String}
     * @return same URI, but with 2 slashes, or null, if {@code storeUri} is null
     */
    public static String replaceFourSlashes(String storeUri) {
        return storeUri == null ? null : storeUri.replaceFirst("////", "//");
    }
}
