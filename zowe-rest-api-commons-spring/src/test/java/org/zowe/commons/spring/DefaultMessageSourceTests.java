/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
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
    public void enableEurekaLoggingTimerTaskDoesNotFails() {
        DefaultMessageSource defaultMessageSource = new DefaultMessageSource(CommonsErrorService.get(), new MockEnvironment().withProperty("apiml.service.serviceId", "testservice").withProperty("server.port", "1234"));
        defaultMessageSource.onApplicationEvent(null);
        assertTrue(CommonsErrorService.get().getDefaultMessageSource().endsWith(":1234:testservice"));
    }
}
