/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.spring.query;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zowe.commons.spring.config.ZoweAuthenticationUtility;
import org.zowe.commons.spring.token.QueryResponse;

import javax.servlet.http.HttpServletRequest;

@Slf4j
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
        //Check if request is sent via HTTP or HTTPS .. The way the request is handled is different in the two cases
        if (request.isSecure()) {
            return queryService.queryHttps(request.getHeader(zoweAuthenticationUtility.getAuthorizationHeader()));
        } else {
            try {
                queryResponse = queryService.query(request);
            } catch (Exception e) {
                log.debug("Error with the http request {}.", e.getMessage());
            }
            return queryResponse;
        }
    }
}
