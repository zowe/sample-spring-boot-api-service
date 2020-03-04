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

import java.lang.management.ManagementFactory;

import org.springframework.stereotype.Component;
import org.zowe.commons.error.CommonsErrorService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ServiceStartupEventHandler {
    public static final int DEFAULT_DELAY_FACTOR = 5;

    public void onServiceStartup(String serviceName, int delayFactor) {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        log.info(CommonsErrorService.get().createApiMessage("org.zowe.commons.service.started", serviceName, uptime / 1000.0).toReadableText());

        new java.util.Timer().schedule(new EnableEurekaLoggingTimerTask(), uptime * delayFactor);
    }
}
