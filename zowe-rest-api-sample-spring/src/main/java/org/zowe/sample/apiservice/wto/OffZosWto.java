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

/**
 * off-z/OS implementation to allow the server to be run off z/OS for testing
 */
@Profile("!zos")
@Service
public class OffZosWto implements Wto {

    public WtoResponse call(int id, String content) {
        int rc = 0;
        String message = "[Mock] Message set from JNI";
        return new WtoResponse(id, content, rc, message);
    }

}
