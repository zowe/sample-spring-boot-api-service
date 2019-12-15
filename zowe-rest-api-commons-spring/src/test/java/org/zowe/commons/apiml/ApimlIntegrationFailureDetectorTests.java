/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.apiml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

import org.junit.Test;
import org.zowe.commons.spring.SpringContext;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.spi.FilterReply;

public class ApimlIntegrationFailureDetectorTests {

    @Test
    public void ApiMediationClientErrorIsDetected() {
        new SpringContext().setApplicationContext(null);
        ApimlIntegrationFailureDetector detector = new ApimlIntegrationFailureDetector();
        assertEquals(FilterReply.NEUTRAL,
                detector.decide(null, null, Level.INFO, null, null, new NullPointerException()));
        assertFalse(detector.reportFatalErrorAndDecideToExit(Level.INFO, new NullPointerException()));
        assertTrue(detector.reportFatalErrorAndDecideToExit(Level.ERROR, new SSLHandshakeException("test")));
        assertTrue(detector.reportFatalErrorAndDecideToExit(Level.ERROR, new SSLPeerUnverifiedException("test")));

        LoggerContext ctx = new LoggerContext();
        Logger logger = ctx.getLogger("com.netflix.DiscoveryClient");
        logger.setLevel(Level.ERROR);
        assertEquals(FilterReply.DENY, detector.decide(null, logger, Level.ERROR,
                null, null, new NullPointerException("test")));

        logger = ctx.getLogger("com.netflix.RedirectingEurekaHttpClient");
        assertEquals(FilterReply.DENY, detector.decide(null, logger, Level.ERROR,
        null, null, new NullPointerException(null)));
    }

}
