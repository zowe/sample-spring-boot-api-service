package org.zowe.sample.apiservice.security;

import io.swagger.annotations.ApiOperation;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationException;

import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {


    private SampleApiAuthenticationProvider service;

    private UserDetailsService userDetailsService;

    @Autowired
    public LoginController(SampleApiAuthenticationProvider service) {
        this.service = service;
    }

    @PostMapping(value = "/login", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "/login", hidden = true)
    public ResponseEntity<AppResponse> login(@RequestBody() LoginRequest loginRequest,
                                             HttpServletResponse response) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
            loginRequest.getPassword());

        try {
            service.authenticate(authenticationToken);

            String token = service.onSuccessfulLoginCreateToken(loginRequest);
            service.setCookie(token, response);

            return ResponseEntity
                .status(HttpStatus.SC_OK)
                .body(new AppResponse("OK", HttpStatus.SC_OK, "User is authenticated"));


        } catch (
            ZosAuthenticationException zosAuthenticationException) {
            return ResponseEntity
                .status(HttpStatus.SC_UNAUTHORIZED)
                .body(new AppResponse("Unauthorised", HttpStatus.SC_UNAUTHORIZED, "User not authenticated"));
        }
    }


    @GetMapping(value = "/query", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "/query", hidden = true)
    public ResponseEntity<QueryResponse> query(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity
            .status(HttpStatus.SC_OK)
            .body(service.parseJwtToken(authorization));
    }
}
