package org.zowe.commons.spring.query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zowe.commons.spring.config.ZoweAuthenticationUtility;
import org.zowe.commons.spring.token.QueryResponse;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1")
public class QueryController {

    @Autowired
    private QueryService queryService;

    @Autowired
    private ZoweAuthenticationUtility zoweAuthenticationUtility;

    QueryResponse queryResponse;

    @GetMapping("/query")
    public QueryResponse queryResponseController(HttpServletRequest request) {

//        return queryService.queryHttps(request.getHeader(zoweAuthenticationUtility.getTokenProperties().getRequestHeader()));

        try {
            queryResponse = queryService.query(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return queryResponse;
    }
}
