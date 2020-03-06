/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.apiml;

import com.ca.mfaas.eurekaservice.client.ApiMediationClient;
import com.ca.mfaas.eurekaservice.client.config.ApiMediationServiceConfig;
import com.ca.mfaas.eurekaservice.client.config.Eureka;
import com.ca.mfaas.eurekaservice.client.config.Ssl;
import com.ca.mfaas.eurekaservice.client.impl.ApiMediationClientImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@EnableConfigurationProperties(value = { ApiMediationServiceConfigBean.class, SslConfigBean.class })
public class RegisterToApiLayer {
    @Autowired
    private ApiMediationServiceConfigBean config;

    @Autowired
    private SslConfigBean ssl;

    @Value("${apiml.service.ipAddress:127.0.0.1}")
    private String ipAddress;

    @Value("${apiml.enabled:false}")
    private boolean enabled;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (enabled) {
            register(config, ssl);
        }
    }

    public void register(ApiMediationServiceConfig config, Ssl ssl) {
        ApiMediationClient apiMediationClient = new ApiMediationClientImpl();
        config.setSsl(ssl);
        config.setEureka(new Eureka(null, null, ipAddress));
        log.info("Registering to API Mediation Layer: baseUrl={}, ipAddress={}, discoveryServiceUrls={}",
                config.getBaseUrl(), config.getEureka().getIpAddress(), config.getDiscoveryServiceUrls());
        log.debug("Registering to API Mediation Layer with settings: {}", config.toString());
        apiMediationClient.register(config);
    }
}
