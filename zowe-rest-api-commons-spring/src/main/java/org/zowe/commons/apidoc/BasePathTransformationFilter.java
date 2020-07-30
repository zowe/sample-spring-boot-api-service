/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.apidoc;

import io.swagger.models.Path;
import io.swagger.models.Swagger;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.swagger2.web.SwaggerTransformationContext;
import springfox.documentation.swagger2.web.WebMvcSwaggerTransformationFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;

// make sure it runs after the default plugin springfox.documentation.swagger2.web.WebMvcBasePathAndHostnameTransformationFilter
@Order(Ordered.HIGHEST_PRECEDENCE + 1000)
@RequiredArgsConstructor
public class BasePathTransformationFilter implements WebMvcSwaggerTransformationFilter {

    private final String basePath;

    @Override
    public Swagger transform(SwaggerTransformationContext<HttpServletRequest> context) {
        Swagger swagger = context.getSpecification();

        String originalBasePath = swagger.getBasePath();
        if (originalBasePath == null) originalBasePath = "";
        swagger.setBasePath(originalBasePath.replaceAll("/$", "") + basePath);

        final Map<String, Path> newPaths = swagger.getPaths().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().replaceAll("^" + basePath, ""),
                        Map.Entry::getValue));
        swagger.setPaths(newPaths);

        return swagger;
    }

    @Override
    public boolean supports(@NonNull DocumentationType delimiter) {
        return delimiter == DocumentationType.SWAGGER_2;
    }

}
