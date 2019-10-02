/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.greeting;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class Greeting {
    @ApiModelProperty(value = "Generated sequence ID of the message")
    private final long id;

    @ApiModelProperty(value = "The greeting message")
    private final String content;
}
