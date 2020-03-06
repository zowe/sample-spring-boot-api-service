/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.spring;

import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

public class EnableEurekaLoggingTimerTask extends TimerTask {
    static final String[] EUREKA_LOGGER_NAMES = new String[] { "com.netflix.discovery.DiscoveryClient",
            "com.netflix.discovery.shared.transport.decorator.RedirectingEurekaHttpClient" };

    @Override
    public void run() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        for (String name : EUREKA_LOGGER_NAMES) {
            Logger logger = loggerContext.getLogger(name);
            logger.setLevel(Level.ERROR);  // NOSONAR: Error level is safe
        }
    }
}
