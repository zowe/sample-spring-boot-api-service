/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sdk.apiml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.net.ssl.SSLHandshakeException;

import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.spi.FilterReply;

public class ApimlIntegrationFailureDetectorTests {

    @Test
    public void ApiMediationClientErrorIsDetected() {
        ApimlIntegrationFailureDetector detector = new ApimlIntegrationFailureDetector();
        assertEquals(FilterReply.NEUTRAL,
                detector.decide(null, null, Level.INFO, null, null, new NullPointerException()));
        assertFalse(detector.shouldExit(Level.INFO, new NullPointerException()));
        assertTrue(detector.shouldExit(Level.ERROR, new SSLHandshakeException("test")));
    }

}
