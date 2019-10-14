/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package jarpatcher;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.ToString;

@ToString
public class ArchiveDefinition {
    @Getter
    private final Map<String, TestFile> files = new LinkedHashMap<>();

    public ArchiveDefinition addFiles(TestFile... testFiles) {
        for (TestFile file: testFiles) {
            files.put(file.getFilename(), file);
        }
        return this;
    }
}
