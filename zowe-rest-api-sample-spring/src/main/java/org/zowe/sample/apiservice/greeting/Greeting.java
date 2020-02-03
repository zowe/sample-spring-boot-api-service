/*
 * This program and the accompanying materials are made available under the terms of the
 * Apache License, Version 2.0 which accompanies this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
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

    @ApiModelProperty(value = "The locale language tag used for this message")
    private final String languageTag;
}
