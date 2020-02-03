/*
 * This program and the accompanying materials are made available under the terms of the
 * Apache License, Version 2.0 which accompanies this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.test;

import org.junit.Before;

public class IntegrationTests {
    protected ServiceUnderTest serviceUnderTest = ServiceUnderTest.getInstance();

    @Before
    public void setup() {
        serviceUnderTest.waitUntilIsReady();
    }
}
