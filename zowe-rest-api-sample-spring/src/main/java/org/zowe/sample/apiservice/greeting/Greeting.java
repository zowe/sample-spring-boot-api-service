/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
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
