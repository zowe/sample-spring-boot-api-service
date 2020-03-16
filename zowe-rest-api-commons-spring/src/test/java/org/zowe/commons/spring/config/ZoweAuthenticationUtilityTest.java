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
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static org.junit.Assert.*;

public class ZoweAuthenticationUtilityTest {

    private static final String KEY_ALIAS = "localhost";
    private static final String JWT_KEY_ALIAS = "jwtsecret";
    private static final String WRONG_PARAMETER = "wrong";
    private static final String PUBLIC_KEY_FILE = "jwt-public-key.pub";

    private HttpsConfig.HttpsConfigBuilder httpsConfigBuilder;

    private Object HttpsConfig;

    @Before
    public void setUp() {
        httpsConfigBuilder = ZoweAuthenticationTestUtility.correctHttpsSettings();
    }

    @Test
    public void testReadSecret() {
        com.ca.mfaas.security.HttpsConfig httpsConfig = httpsConfigBuilder.keyAlias(KEY_ALIAS).build();
        String secretKey = ZoweAuthenticationUtility.readSecret(httpsConfig);
        assertNotNull(secretKey);
    }

    @Test(expected = HttpsConfigError.class)
    public void testReadSecretWithIncorrectKeyAlias() {
        HttpsConfig httpsConfig = httpsConfigBuilder.keyAlias(WRONG_PARAMETER).build();
        String secretKey = ZoweAuthenticationUtility.readSecret(httpsConfig);
        assertNull(secretKey);
    }

    @Test
    public void testLoadKey() {
        HttpsConfig httpsConfig = httpsConfigBuilder.keyAlias(JWT_KEY_ALIAS).build();
        Key secretKey = ZoweAuthenticationUtility.loadKey(httpsConfig);
        assertNotNull(secretKey);
    }

    @Test
    public void testFindFirstSecretKey() throws KeyStoreException, NoSuchAlgorithmException, IOException, CertificateException {
        HttpsConfig httpsConfig = httpsConfigBuilder.keyAlias(JWT_KEY_ALIAS).build();
        KeyStore ks = ZoweAuthenticationUtility.loadKeyStore(httpsConfig);
        char[] keyPasswordInChars = httpsConfig.getKeyPassword() == null ? null : httpsConfig.getKeyPassword().toCharArray();
        Key key = ZoweAuthenticationUtility.findFirstSecretKey(ks, keyPasswordInChars);
        assertNotNull(key);
    }

    @Test
    public void testGetJwtSecret() {
        assertNull(new ZoweAuthenticationUtility().getJwtSecret());
    }

    @Test(expected = HttpsConfigError.class)
    public void testLoadKeyWithIncorrectKeyPassword() {
        HttpsConfig httpsConfig = httpsConfigBuilder.keyAlias(JWT_KEY_ALIAS).keyPassword(WRONG_PARAMETER).build();
        Key secretKey = ZoweAuthenticationUtility.loadKey(httpsConfig);
        assertNull(secretKey);
    }

    @Test
    public void testLoadKeyStore() {
        HttpsConfig httpsConfig = httpsConfigBuilder.build();
        try {
            KeyStore keyStore = ZoweAuthenticationUtility.loadKeyStore(httpsConfig);
            assertTrue(keyStore.size() > 0);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test(expected = MalformedURLException.class)
    public void testKeyRingUrl() throws MalformedURLException {
        HttpsConfig httpsConfig = httpsConfigBuilder.build();
        assertNotNull(ZoweAuthenticationUtility.keyRingUrl(httpsConfig.getKeyStore(), httpsConfig.getTrustStore()));
    }

    @Test
    public void testReplaceFourSlashes() {
        String newUrl = ZoweAuthenticationUtility.replaceFourSlashes("safkeyring:////userId/keyRing");
        assertEquals("safkeyring://userId/keyRing", newUrl);
        String newUrl2 = ZoweAuthenticationUtility.replaceFourSlashes(null);
        assertNull(newUrl2);
    }
}

