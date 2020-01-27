package org.zowe.sample.apiservice.security;

import io.swagger.annotations.ApiOperation;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zowe.commons.zos.security.authentication.ZosAuthenticationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.zowe.sample.apiservice.security.SampleApiAuthenticationProvider.decode;

@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {


    private SampleApiAuthenticationProvider service;

    private UserDetailsService userDetailsService;

    @Autowired
    public LoginController(SampleApiAuthenticationProvider service,
                           UserDetailsService userDetailsService) {
        this.service = service;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping(value = "/login", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "/login", hidden = true)
    public ResponseEntity<AppResponse> login(@RequestHeader("Authorization") String authorization, HttpServletResponse response)
        throws IOException, ServletException {

        UserDetails user = userDetailsService.loadUserByUsername(authorization);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getUsername(),
            decode(user.getPassword()));

        try {
            service.authenticate(authenticationToken);

            String token = service.successfulAuthentication(user);
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


    //TODO: This need to be implemented from scratch
    @GetMapping(value = "/query", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "/query", hidden = true)
    public ResponseEntity<AppResponse> query(@RequestHeader("Authorization") String authorization, HttpServletResponse response) {
        return ResponseEntity
            .status(HttpStatus.SC_UNAUTHORIZED)
            .body(new AppResponse("Unauthorised", HttpStatus.SC_UNAUTHORIZED, "User not authenticated"));
    }
}
