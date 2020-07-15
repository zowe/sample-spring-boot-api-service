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

import lombok.extern.slf4j.Slf4j;
import springfox.documentation.spring.web.paths.DefaultPathProvider;

/**
 * @deprecated This class does not correctly work with SpringFox 3.x
 * A base path is not modified although paths for each endpoint are changed
 *
 * Use {@link org.zowe.commons.apidoc.BasePathTransformationFilter} instead
 *
 */
@Deprecated
@Slf4j
public class BasePathProvider extends DefaultPathProvider {
    private final String basePath;

    public BasePathProvider(String basePath) {
        this.basePath = basePath;
        log.warn("BasePathProvider class is deprecated and does not work correctly. Use `org.zowe.commons.apidoc.BasePathTransformationFilter` instead.");
    }

    @Override
    public String getOperationPath(String operationPath) {
        String newOperationPath = super.getOperationPath(operationPath);
        if (newOperationPath.startsWith(basePath)) {
            return newOperationPath.substring(basePath.length());
        } else {
            return newOperationPath;
        }
    }

    @Override
    protected String getDocumentationPath() {
        return basePath + "/apiDocs";
    }
}
