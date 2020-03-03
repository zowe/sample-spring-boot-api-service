/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TokenControllerTest {

    @InjectMocks
    private static TokenController tokenController;

    @Mock
    TokenService tokenService;

    @Mock
    HttpServletResponse httpServletResponse;

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
