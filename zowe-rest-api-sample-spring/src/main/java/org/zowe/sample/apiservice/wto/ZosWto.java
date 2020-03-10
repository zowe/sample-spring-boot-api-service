/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.wto;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.zowe.commons.zos.LibLoader;

import static org.zowe.sample.apiservice.AppNativeLibraries.SAMPLE_LIBRARY_NAME;

/**
 * z/OS implementation calling the native, OS-linkage service WTO via a "shared
 * object" loaded at server runtime.
 */
@Profile("zos")
@Service
public class ZosWto implements Wto {
    static {
        new LibLoader().loadLibrary(SAMPLE_LIBRARY_NAME);
    }

    private native int wto(int id, String content);

    private String message; // String class variable set in JNI code

    public WtoResponse call(int id, String content) {
        int rc = wto(id, content);
        return new WtoResponse(id, content, rc, message);
    }
}
