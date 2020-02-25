/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
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
