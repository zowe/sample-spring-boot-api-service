# Web Security

## Authentication

The REST API service is protected by **HTTP Basic authentication** that is connected to `ZosAuthenticationProvider`.

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

## Implementation

More details are provided at [z/OS Security](zos-security.md).

## Future Direction

The sample and SDK supports only the HTTP Basic authentication at this moment.
HTTP Basic authentication will be always supported since it is the most easiest method supported by all clients.

In order to support multi-factor authentication, the SDK and sample will support **stateless token-based authentication** in future.

It will use following services:

- [Zowe Authentication and Authorization Service](https://github.com/zowe/api-layer/wiki/Zowe-Authentication-and-Authorization-Service)
- possibly other token-based security services such as z/OSMF

It will be controlled by a configuration property (no or minimal change in Java code will be required).
