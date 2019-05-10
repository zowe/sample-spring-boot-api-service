/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.apidoc;

import static org.zowe.sample.apiservice.apidoc.ApiDocConstants.DOC_SCHEME_BASIC_AUTH;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.BasicAuth;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig implements WebMvcConfigurer {

    @Value("${apiml.service.apiInfo[0].title}")
    private String apiTitle;

    @Value("${apiml.service.apiInfo[0].version}")
    private String apiVersion;

    @Value("${apiml.service.apiInfo[0].description}")
    private String apiDescription;

    @Bean
    public Docket api() {
        List<SecurityScheme> schemes = new ArrayList<>();
        schemes.add(new BasicAuth(DOC_SCHEME_BASIC_AUTH));

        return new Docket(DocumentationType.SWAGGER_2).groupName("v1").select().apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.ant("/api/v1/*")).build().apiInfo(apiInfo()).securitySchemes(schemes)
                .pathProvider(new BasePathProvider("/api/v1"));
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(apiTitle, apiDescription, apiVersion, null, null, null, null, new ArrayList<>());
    }

    // @Bean
    // public SecurityConfiguration security() {
    // return
    // SecurityConfigurationBuilder.builder().useBasicAuthenticationWithAccessCodeGrant(true).build();
    // }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/api/v1/apiDocs", "/apiDocs/v2?group=v1");
        registry.addRedirectViewController("/", "swagger-ui.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
