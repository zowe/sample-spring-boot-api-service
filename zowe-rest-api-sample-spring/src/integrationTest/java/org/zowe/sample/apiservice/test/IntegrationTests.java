/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
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
