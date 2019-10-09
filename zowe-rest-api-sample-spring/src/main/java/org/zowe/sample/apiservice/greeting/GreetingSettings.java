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
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GreetingSettings {
    @ApiModelProperty(value = "The greeting message without the name")
    private String greeting;
}
