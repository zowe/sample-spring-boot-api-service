/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
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
