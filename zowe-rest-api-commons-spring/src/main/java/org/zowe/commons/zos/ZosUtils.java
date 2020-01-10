/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.zos;

public class ZosUtils {
    private ZosUtils() {
        // no instances
    }

    /**
     * @return True when running on z/OS.
     */
    public static boolean isRunningOnZos() {
        String osName = System.getProperty("os.name");
        return "z/OS".equals(osName);
    }
}
