/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sdk.zos;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LibLoader {
    public String libraryFileName(String libraryName) {
        return "lib" + libraryName + ".so";
    }

    public void loadLibrary(String libraryName) {
        try {
            System.loadLibrary(libraryName);
        } catch (UnsatisfiedLinkError e) {

            log.error("Could not load native library (shared object) '{}. "
                    + "Check that the library file is present in a directory of Java library path. "
                    + "The Java library path is set by LIBPATH environment variable or java.library.path property. "
                    + "Check that the library file has program controlled attribute and is executable. "
                    + "java.library.path={}", libraryFileName(libraryName), System.getProperty("java.library.path"));
            throw e;
        }
    }

}
