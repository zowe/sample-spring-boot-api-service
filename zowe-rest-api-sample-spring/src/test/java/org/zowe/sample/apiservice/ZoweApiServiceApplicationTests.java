/*
 * This program and the accompanying materials are made available under the terms of the
 * Apache License, Version 2.0 which accompanies this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.context.junit4.SpringRunner;
import org.zowe.commons.spring.security.CsrfController;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ZoweApiServiceApplicationTests {

    @Test
    public void csrfControllerReturnsCsrfToken() {
        CsrfController controller = new CsrfController();
        CsrfToken token = new DefaultCsrfToken("headerName", "parameterName", "token");
        assertEquals(token, controller.csrf(token));
    }
}
