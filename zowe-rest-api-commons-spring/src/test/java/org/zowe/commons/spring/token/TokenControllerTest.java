/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.spring.token;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TokenControllerTest {

    @InjectMocks
    private static TokenController tokenController;

    @Mock
    TokenService tokenService;

    MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();

    @Mock
    HttpServletRequest httpServletRequest;

    @Test
    public void verifyLogin() throws Exception {
        when(tokenService.login(new LoginRequest("zowe", "zowe"), httpServletRequest, httpServletResponse)).
            thenReturn("token");

        tokenController.login(new LoginRequest("zowe", "zowe"), httpServletRequest, httpServletResponse);

        Assert.assertEquals(204, httpServletResponse.getStatus());
    }

    @Test
    public void unauthorizedLogin() throws Exception {
        tokenController.login(new LoginRequest("zowe", "dsfds"), httpServletRequest, httpServletResponse);

        Assert.assertEquals(401, httpServletResponse.getStatus());
    }

    @Test
    public void testVerifyLogin() throws Exception {
        when(tokenService.query(httpServletRequest)).thenReturn(new QueryResponse("user", new Date(), new Date()));

        Assert.assertNotNull(tokenController.queryResponseController(httpServletRequest));
    }

    @Test
    public void catchException() throws Exception {
        when(tokenService.query(httpServletRequest)).thenThrow(new RuntimeException());

        Assert.assertNull(tokenController.queryResponseController(httpServletRequest));
    }
}
