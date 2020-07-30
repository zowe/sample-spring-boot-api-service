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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import springfox.documentation.swagger2.web.SwaggerTransformationContext;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class BasePathTransformationFilterTest {

    private final Swagger swagger = new Swagger();
    private final BasePathTransformationFilter basePathTransformationFilter = new BasePathTransformationFilter("/api/v1");

    @Mock
    private SwaggerTransformationContext<HttpServletRequest> swaggerTransformationContext;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        when(swaggerTransformationContext.getSpecification()).thenReturn(swagger);

        swagger.setPaths(new LinkedHashMap<>());
        swagger.getPaths().put("/api/v1/test1", new Path());
    }

    @Test
    void swaggerIsCorrectlyTransformed() {
        swagger.setBasePath("/contextPath");
        swagger.getPaths().put("/api/test2", new Path());
        swagger.getPaths().put("/test3/api/v1", new Path());
        swagger.getPaths().put("/api/v1/test4", new Path());

        basePathTransformationFilter.transform(swaggerTransformationContext);

        assertEquals("/contextPath/api/v1", swagger.getBasePath());
        assertTrue(swagger.getPaths().containsKey("/test1"));
        assertTrue(swagger.getPaths().containsKey("/api/test2"));
        assertTrue(swagger.getPaths().containsKey("/test3/api/v1"));
        assertTrue(swagger.getPaths().containsKey("/test4"));
    }

    @Test
    void noBasePathIsPresent() {
        basePathTransformationFilter.transform(swaggerTransformationContext);

        assertEquals("/api/v1", swagger.getBasePath());
        assertTrue(swagger.getPaths().containsKey("/test1"));
    }

}