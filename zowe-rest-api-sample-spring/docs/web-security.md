# Web Security

## Authentication

The REST API service is protected by Spring security. It now supports **stateless token-based authentication**.

The REST API endpoints can be authenticated now either by Basic Auth OR with Bearer token OT you can provide the JWT token as a cookie in the request.
No changes are required in the REST API endpoints which are developed on top of ZOWE SDK. Internally `ZoweWebSecurityConfig` will take care of all incoming requests and check if
the user has provided valid Basic Auth OR JWT token as bearer token/Cookie in the request

The real protection by the authentication is configured in `ZoweWebSecurityConfig` class:

```java
.and()
.addFilterBefore(new AuthorizationFilter(tokenFailureHandler, authConfigurationProperties, tokenService), UsernamePasswordAuthenticationFilter.class);
```

## Implementation

More details are provided at [z/OS Security](zos-security.md).

## Future Direction

The sample and SDK supports HTTP Basic authentication and **stateless token-based authentication** at this moment.

The SDK and sample will support other token-based security services such as z/OSMF in future.

