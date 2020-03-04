/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
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
