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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ZoweApiServiceApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Test
    public void csrfControllerReturnsCsrfToken() {
        CsrfController controller = new CsrfController();
        CsrfToken token = new DefaultCsrfToken("headerName", "parameterName", "token");
        assertEquals(token, controller.csrf(token));
    }

}
