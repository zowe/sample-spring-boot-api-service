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

import static org.junit.Assert.assertTrue;
import static org.zowe.commons.spring.EnableEurekaLoggingTimerTask.EUREKA_LOGGER_NAMES;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

public class EnableEurekaLoggingTimerTaskTests {
    SpringContext context = new SpringContext();

    @Test
    public void enableEurekaLoggingTimerTaskDoesNotFail() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        new EnableEurekaLoggingTimerTask().run();
        for (String name : EUREKA_LOGGER_NAMES) {
            assertTrue(loggerContext.getLogger(name).isErrorEnabled());
        }
    }
}
