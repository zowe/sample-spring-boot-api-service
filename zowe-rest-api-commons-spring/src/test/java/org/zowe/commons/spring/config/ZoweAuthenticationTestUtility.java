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
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.fail;

@Slf4j
public class ZoweAuthenticationTestUtility {

    private static final String STORE_PASSWORD = "password"; // NOSONAR

    public static HttpsConfig.HttpsConfigBuilder correctHttpsSettings() {
        return ZoweAuthenticationTestUtility.correctHttpsKeyStoreSettings()
            .trustStore(pathFromRepository("localhost.truststore.p12"))
            .trustStorePassword(STORE_PASSWORD);
    }

    public static HttpsConfig.HttpsConfigBuilder correctHttpsKeyStoreSettings() {
        return HttpsConfig.builder().protocol("TLSv1.2")
            .keyStore(ZoweAuthenticationTestUtility.pathFromRepository("localhost.keystore.p12"))
            .keyStorePassword(STORE_PASSWORD).keyPassword(STORE_PASSWORD);
    }

    public static String pathFromRepository(String path) {
        String newPath = /*"../" + */"src/test/resources/" + path;
        try {
            return new File(newPath).getCanonicalPath();
        } catch (IOException e) {
//            log.error("Error opening file {}", newPath, e);
            fail("Invalid repository path: " + newPath);
            return null;
        }
    }
}
