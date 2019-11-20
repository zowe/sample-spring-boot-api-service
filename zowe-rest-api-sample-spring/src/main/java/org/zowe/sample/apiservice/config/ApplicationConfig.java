/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.zowe.commons.error.ErrorService;
import org.zowe.commons.error.ErrorServiceImpl;
import org.zowe.commons.spring.ServiceStartupEventHandler;

@Configuration
@ComponentScan("org.zowe.commons")
public class ApplicationConfig implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${apiml.service.title}")
    private String serviceTitle;

    @Autowired
    private ServiceStartupEventHandler serviceStartupEventHandler;

    private final ErrorService errorService = ErrorServiceImpl.getDefault();

    @Bean
    public ErrorService errorService() {
        return errorService;
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        serviceStartupEventHandler.onServiceStartup(serviceTitle, ServiceStartupEventHandler.DEFAULT_DELAY_FACTOR);
    }
}
