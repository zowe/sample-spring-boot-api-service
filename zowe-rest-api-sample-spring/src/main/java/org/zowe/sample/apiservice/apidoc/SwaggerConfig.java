/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.apidoc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.zowe.commons.apidoc.BasePathProvider;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.BasicAuth;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

import static org.zowe.commons.apidoc.ApiDocConstants.DOC_SCHEME_BASIC_AUTH;

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

        return new Docket(DocumentationType.SWAGGER_2)
            .useDefaultResponseMessages(false)
            .groupName("v1").select()
            .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.ant("/api/v1/**"))
            .build().apiInfo(apiInfo())
            .securitySchemes(schemes)
            .pathProvider(new BasePathProvider("/api/v1"));
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(apiTitle, apiDescription, apiVersion, null, null, null, null, new ArrayList<>());
    }

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
