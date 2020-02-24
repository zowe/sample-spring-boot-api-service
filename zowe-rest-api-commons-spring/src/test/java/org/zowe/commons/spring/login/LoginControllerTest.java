package org.zowe.commons.spring.login;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.zowe.commons.spring.token.TokenService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoginControllerTest {

    @InjectMocks
    private static LoginController loginController;

    @Mock
    TokenService tokenService;

    @Mock
    HttpServletResponse httpServletResponse;

    @Mock
    HttpServletRequest httpServletRequest;

    @Test
    public void verifyLogin() throws Exception {
        when(tokenService.login(new LoginRequest("zowe", "zowe"), httpServletRequest, httpServletResponse)).
            thenReturn(new ResponseEntity(HttpStatus.OK));

        loginController.login(new LoginRequest("zowe", "zowe"), httpServletRequest, httpServletResponse);
    }
}
