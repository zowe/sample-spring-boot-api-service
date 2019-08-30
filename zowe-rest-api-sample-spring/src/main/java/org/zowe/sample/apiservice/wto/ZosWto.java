/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.wto;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static org.zowe.sample.apiservice.AppNativeLibraries.WTO_LIBRARY_NAME;

/**
 * z/OS implementation calling the native, OS-linkage service WTO via
 * a "shared object" loaded at server runtime.
 */
@Profile("zos")
@Service
public class ZosWto implements Wto {
    static {
        System.loadLibrary(WTO_LIBRARY_NAME);
    }

    private native int wto(int id, String content);

    private String message; // String class variable set in JNI code

    public WtoResponse call(int id, String content) {
        int rc = wto(id, content);
        return new WtoResponse(id, content, rc, message);
    }
}
