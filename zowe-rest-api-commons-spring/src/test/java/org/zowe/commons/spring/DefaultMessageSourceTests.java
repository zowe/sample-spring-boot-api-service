/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.spring;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;
import org.zowe.commons.error.CommonsErrorService;

public class DefaultMessageSourceTests {
    @Test
    public void defaultMessageSourceContainsPortAndServiceName() {
        DefaultMessageSource defaultMessageSource = new DefaultMessageSource(CommonsErrorService.get(), new MockEnvironment().withProperty("apiml.service.serviceId", "testservice").withProperty("server.port", "1234"));
        defaultMessageSource.onApplicationEvent(null);
        assertTrue(CommonsErrorService.get().getDefaultMessageSource().endsWith(":1234:testservice"));
    }
}
