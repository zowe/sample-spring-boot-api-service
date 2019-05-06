package org.zowe.sample.apiservice.config;

import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebServerSecurityConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainerCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            AbstractHttp11Protocol<?> abstractProtocol = (AbstractHttp11Protocol<?>) connector.getProtocolHandler();
            abstractProtocol.setUseServerCipherSuitesOrder(true);
        });
    }
}
