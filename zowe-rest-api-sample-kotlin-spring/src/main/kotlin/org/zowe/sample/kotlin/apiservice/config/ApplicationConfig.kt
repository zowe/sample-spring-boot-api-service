/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.kotlin.apiservice.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.zowe.apiml.enable.EnableApiDiscovery

@Configuration
@EnableApiDiscovery
@ComponentScan(
    basePackages = ["org.zowe.commons.spring"],
    useDefaultFilters = false,
    includeFilters = [ComponentScan.Filter(type = FilterType.REGEX,
        pattern = [
            "org.zowe.commons.spring.CustomRestExceptionHandler",
            "org.zowe.commons.spring.WebConfig"
        ]
    )]
)
class ApplicationConfig {
}