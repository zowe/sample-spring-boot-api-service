/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice;

import com.ca.mfaas.error.ErrorService;
import com.ca.mfaas.error.impl.ErrorServiceImpl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ZoweApiServiceApplication {
    @Bean
    public ErrorService errorService() {
        return new ErrorServiceImpl("/messages.yml");
    }
    public static void main(String[] args) {
		SpringApplication.run(ZoweApiServiceApplication.class, args);
    }

}
