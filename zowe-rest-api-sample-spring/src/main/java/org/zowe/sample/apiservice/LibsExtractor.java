/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.zowe.sdk.zos.LibExtractor;
import org.zowe.sdk.zos.SdkNativeLibraries;

public class LibsExtractor {
    public static void main(String[] args) throws URISyntaxException, IOException {
        LibExtractor ex = new LibExtractor();
        List<String> libraries = new ArrayList<>();
        libraries.addAll(new AppNativeLibraries().getNativeLibrariesNames());
        libraries.addAll(new SdkNativeLibraries().getNativeLibrariesNames());
        if (args.length == 1) {
            for(String library: libraries) {
                ex.extractLibrary(library, args[0]);
            }
        } else {
            System.err.println("No arguments provided. Expected: targetDir");
        }
    }
}