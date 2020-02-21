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

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zowe.commons.rest.response.ApiMessage;
import org.zowe.commons.spring.config.ZoweAuthenticationUtility;
import org.zowe.commons.spring.token.AppResponse;
import org.zowe.commons.spring.token.QueryResponse;

import javax.servlet.http.HttpServletRequest;

import static org.zowe.commons.apidoc.ApiDocConstants.DOC_SCHEME_BASIC_AUTH;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@Api(tags = "Query JWT Token")
public class QueryController {

    @Autowired
    private QueryService queryService;

    @Autowired
    private ZoweAuthenticationUtility zoweAuthenticationUtility;

    QueryResponse queryResponse;

    @GetMapping("/query")
    @ApiOperation(value = "This API is used to return details of JWT token like Username, Issued Time and Expiration Time.", nickname = "query", authorizations = {
        @Authorization(value = DOC_SCHEME_BASIC_AUTH)})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful Validation", response = QueryResponse.class),
        @ApiResponse(code = 401, message = "The request has not been applied because it lacks valid authentication credentials for the target resource", response = ApiMessage.class)})
    public QueryResponse queryResponseController(HttpServletRequest request) {
        try {
            queryResponse = queryService.query(request);
        } catch (Exception e) {
            log.debug("Error with the http request {}.", e.getMessage());
        }
        return queryResponse;
    }
}
