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

import org.apache.commons.lang3.exception.ExceptionUtils;

import org.slf4j.Marker;
import org.zowe.sdk.spring.SpringContext;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApimlIntegrationFailureDetector extends TurboFilter {

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        if (level.isGreaterOrEqual(Level.ERROR)) {
            if (ExceptionUtils.indexOfType(t, javax.net.ssl.SSLHandshakeException.class) >= 0) {
                for (String s : ExceptionUtils.getStackFrames(t)) {
                    if (s.indexOf(".ApiMediationClient") >= 0) {
                        log.error(
                            "Unable to connect to Zowe API Mediation Layer. The certificate of your service is not trusted by the API Mediation Layer: {}",
                            t.getMessage());
                        if (SpringContext.getApplicationContext() == null) {
                            System.exit(1);
                        }
                    }
                }
            }
        }

        return FilterReply.NEUTRAL;
    }

}