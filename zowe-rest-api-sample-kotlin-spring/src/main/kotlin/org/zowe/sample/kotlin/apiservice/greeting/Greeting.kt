/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.kotlin.apiservice.greeting

import io.swagger.v3.oas.annotations.media.Schema

data class Greeting(

    @Schema(description = "Generated sequence ID of the message")
    val id: Long,

    @Schema(description = "The greeting message")
    val content: String,

    @Schema(description = "The locale language tag used for this message")
    val languageTag: String
)
