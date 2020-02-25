/*
 * This program and the accompanying materials are made available under the terms of the
 * Apache License, Version 2.0 which accompanies this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice;

import java.util.ArrayList;
import java.util.List;

import org.zowe.commons.zos.NativeLibraries;

public class AppNativeLibraries implements NativeLibraries {
    public static final String SAMPLE_LIBRARY_NAME = "zowe-sample";

    @Override
    public List<String> getNativeLibrariesNames() {
        List<String> libraries = new ArrayList<>();
        libraries.add(SAMPLE_LIBRARY_NAME);
        return libraries;
    }
}
