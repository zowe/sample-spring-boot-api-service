/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.spring.token;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Represents the query JSON response with the token information
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryResponse {
    @ApiModelProperty(value = "User ID of the user who is logged in.")
    private String userId;

    @ApiModelProperty(value = "Time when the token was generated.")
    private Date creation;

    @ApiModelProperty(value = "Expiration Time of the token.")
    private Date expiration;
}
