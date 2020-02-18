/*
 * This program and the accompanying materials are made available under the terms of the
 * Apache License, Version 2.0 which accompanies this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.wto;

import lombok.Data;

/**
 * Class to model the data returned from the /wto endpoint
 */
@Data
public class WtoResponse {
    private final int id;
    private final String content;
    private final int rc;
    private final String message;
}
