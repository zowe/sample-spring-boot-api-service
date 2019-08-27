# z/OS Security

## Authentication

The REST API service is protected by HTTP Basic authentication that is connected to `ZosAuthenticationProvider`.

The REST API endpoints that require HTTP Basic authentication have declare it in the `@ApiOperation` annotation.
This annotation is used only for API documentation.

    ```java
    import static org.zowe.sample.apiservice.apidoc.ApiDocConstants.DOC_SCHEME_BASIC_AUTH;

    @ApiOperation(..., authorizations = { @Authorization(value = DOC_SCHEME_BASIC_AUTH) })
    @GetMapping("/greeting")
    public Greeting greeting(
    ```

The real protection by the authentication is configured in `WebSecurityConfig` class:

    ```java
    http.authorizeRequests().anyRequest().authenticated();
    ```
