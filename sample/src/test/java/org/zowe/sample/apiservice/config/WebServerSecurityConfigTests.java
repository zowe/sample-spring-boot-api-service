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

import static org.mockito.Mockito.withSettings;

import java.util.List;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;

public class WebServerSecurityConfigTests {

    @Test
    public void servletContainerCustomizerForcesServerCipherSuitesOrder() {
        WebServerSecurityConfig webServerSecurityConfig = new WebServerSecurityConfig();
        WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainerCustomizer = webServerSecurityConfig
                .servletContainerCustomizer();

        TomcatServletWebServerFactory factory = Mockito.mock(TomcatServletWebServerFactory.class,
                withSettings().verboseLogging());
        servletContainerCustomizer.customize(factory);
        ArgumentCaptor<TomcatConnectorCustomizer> customizerCaptor = ArgumentCaptor
                .forClass(TomcatConnectorCustomizer.class);
        Mockito.verify(factory).addConnectorCustomizers(customizerCaptor.capture());
        List<TomcatConnectorCustomizer> customizers = customizerCaptor.getAllValues();

        Connector connector = Mockito.mock(Connector.class, withSettings().verboseLogging());
        AbstractHttp11Protocol<?> protocol = Mockito.mock(AbstractHttp11Protocol.class);
        Mockito.when(connector.getProtocolHandler()).thenReturn(protocol);

        for (TomcatConnectorCustomizer customizer : customizers) {
            customizer.customize(connector);
        }

        Mockito.verify(protocol).setUseServerCipherSuitesOrder(true);
    }

}
