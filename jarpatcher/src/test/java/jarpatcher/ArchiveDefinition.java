/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
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
