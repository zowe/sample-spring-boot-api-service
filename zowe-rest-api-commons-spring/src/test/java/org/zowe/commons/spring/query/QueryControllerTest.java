package org.zowe.commons.spring.query;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.zowe.commons.spring.token.QueryResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QueryControllerTest {

    @InjectMocks
    private static QueryController queryController;

    @Mock
    QueryService queryService;

    @Mock
    HttpServletRequest httpServletRequest;

    @Test
    public void testVerifyLogin() throws Exception {
        when(queryService.query(httpServletRequest)).thenReturn(new QueryResponse("user", new Date(), new Date()));

        queryController.queryResponseController(httpServletRequest);
    }

    @Test
    public void catchException() throws Exception {
        when(queryService.query(httpServletRequest)).thenThrow(new RuntimeException());

        queryController.queryResponseController(httpServletRequest);
    }
}
