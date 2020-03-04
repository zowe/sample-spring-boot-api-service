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

import springfox.documentation.spring.web.paths.AbstractPathProvider;

public class BasePathProvider extends AbstractPathProvider {
    private final String basePath;

    public BasePathProvider(String basePath) {
        this.basePath = basePath;
    }

    @Override
    protected String applicationPath() {
        return basePath;
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
