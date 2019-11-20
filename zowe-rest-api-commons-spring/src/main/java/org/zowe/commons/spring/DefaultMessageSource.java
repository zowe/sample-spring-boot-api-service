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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.zowe.commons.error.CommonsErrorService;
import org.zowe.commons.error.ErrorService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class DefaultMessageSource implements ApplicationListener<ApplicationReadyEvent> {
    private final Environment environment;
    private final ErrorService errorService;

    @Autowired
    public DefaultMessageSource(ErrorService errorService, Environment environment) {
        this.errorService = errorService;
        this.environment = environment;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
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
