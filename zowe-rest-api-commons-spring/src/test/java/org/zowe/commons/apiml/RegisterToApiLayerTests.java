/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.apiml;

import java.security.InvalidParameterException;

import com.ca.mfaas.eurekaservice.client.config.ApiMediationServiceConfig;
import com.ca.mfaas.eurekaservice.client.config.Ssl;

import org.junit.Test;

public class RegisterToApiLayerTests {

    @Test(expected = InvalidParameterException.class)

    public void emptyConfigShouldFail() {
        ApiMediationServiceConfig config = new ApiMediationServiceConfig();
        Ssl ssl = new Ssl();
        new RegisterToApiLayer().register(config, ssl);
    }

}
