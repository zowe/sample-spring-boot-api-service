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

/**
 * Class to model the data returned from the /wto endpoint
 */
public class WtoResponse {

    private final int rc;
    private final String message;
    private final int id;
    private final String content;

    public WtoResponse(int id, String content, int rc, String message) {
        this.id = id;
        this.content = content;
        this.rc = rc;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public int getRc() {
        return rc;
    }

    public String getMessage() {
        return message;
    }
}
