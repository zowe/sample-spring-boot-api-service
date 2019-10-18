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

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.zowe.commons.error.CommonsErrorService;
import org.zowe.commons.error.ErrorService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ServiceStartupEventHandler {
    public static int DEFAULT_DELAY_FACTOR = 5;

    @Autowired
    private Environment environment;

    public void onServiceStartup(String serviceName, int delayFactor) {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        log.info("{} has been started in {} seconds", serviceName, uptime / 1000.0);

        new java.util.Timer().schedule(new EnableEurekaLoggingTimerTask(), uptime * DEFAULT_DELAY_FACTOR);

        setDefaultErrorMessageSource();
    }

    private void setDefaultErrorMessageSource() {
        ErrorService errorService = SpringContext.getBean(ErrorService.class);
        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            log.warn("Could not obtain hostname", e);
            hostname = "?";
        }
        String serviceId = environment.getProperty("apiml.service.serviceId");
        String messageSource = String.format("%s:%s:%s", hostname, environment.getProperty("server.port"), serviceId);
        errorService.setDefaultMessageSource(messageSource);
        CommonsErrorService.get().setDefaultMessageSource(messageSource);
    }
}
