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

import com.netflix.discovery.shared.Application;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.zowe.apiml.eurekaservice.client.ApiMediationClient;
import org.zowe.apiml.eurekaservice.client.config.ApiMediationServiceConfig;
import org.zowe.apiml.eurekaservice.client.config.Ssl;
import org.zowe.apiml.eurekaservice.client.impl.ApiMediationClientImpl;
import org.zowe.apiml.exception.ServiceDefinitionException;

@Component
@Slf4j
@EnableConfigurationProperties(value = { ApiMediationServiceConfigBean.class, SslConfigBean.class })
@RequiredArgsConstructor
public class RegisterToApiLayer {
    private final ApiMediationServiceConfigBean config;
    private final SslConfigBean ssl;
    private final CatalogUiTile catalogUiTile;

    @Value("${apiml.service.ipAddress:127.0.0.1}")
    private String ipAddress;

    @Value("${apiml.enabled:false}")
    @Getter
    private boolean enabled;

    @Getter
    private ApiMediationClient apiMediationClient;

    private String apiGatewayBaseUrl;

    public Ssl getSsl() {
        return ssl;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (enabled) {
            register(config, ssl);
        }
    }

    public void register(ApiMediationServiceConfig config, Ssl ssl) {
        apiMediationClient = createApiMediationClient();
        config.setSsl(ssl);
        config.setServiceIpAddress(ipAddress);
        convertOldApimlConfiguration(config);
        log.info("Registering to API Mediation Layer: baseUrl={}, discoveryServiceUrls={}", config.getBaseUrl(),
                config.getDiscoveryServiceUrls());
        log.debug("Registering to API Mediation Layer with settings: {}", config.toString());
        try {
            apiMediationClient.register(config);
        } catch (ServiceDefinitionException e) {
            log.info(
                    "Registering to API Mediation Layer failed: {baseUrl={}, ipAddress={}, discoveryServiceUrls={}} failed with exception {}",
                    config.getBaseUrl(), config.getServiceIpAddress(), config.getDiscoveryServiceUrls(),
                    e.getMessage());
            log.debug(String.format("Service %s registration to API ML failed: ", config.getBaseUrl()), e);
        }
    }

    public String getApiGatewayBaseUrl() {
        if ((apiGatewayBaseUrl == null) && (apiMediationClient != null)) {
            Application gateway = apiMediationClient.getEurekaClient().getApplication("gateway");
            if ((gateway != null) && !gateway.getInstances().isEmpty()) {
                apiGatewayBaseUrl = gateway.getInstances().get(0).getHomePageUrl();
                log.info("Using API Gateway at {}", apiGatewayBaseUrl);
            }
        }
        return apiGatewayBaseUrl;
    }

    /**
     * @deprecated remove when Sample 2.0 is released
     */
    @Deprecated
    private void convertOldApimlConfiguration(ApiMediationServiceConfig config) {
        if (config.getCatalog() == null) {
            if (catalogUiTile != null) {
                config.setCatalog(catalogUiTile.toCatalog());
                log.warn("The old version of the Zowe API ML configuration detected. "
                        + "Please change the `catalogUiTile` parameter to `catalog` format according the Zowe API ML documentation. "
                        + "See: https://docs.zowe.org/stable/extend/extend-apiml/onboard-plain-java-enabler.html#api-catalog-information");
            } else {
                log.warn("No catalog configuration detected. The service will not be visible in API Catalog");
            }
        }
    }

    protected ApiMediationClient createApiMediationClient() {
        return new ApiMediationClientImpl();
    }

}
