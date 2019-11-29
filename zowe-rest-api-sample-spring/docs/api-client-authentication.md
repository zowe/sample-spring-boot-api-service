# API Client Authentication

- [API Client Authentication](#api-client-authentication)
  - [HTTP Basic Authentication](#http-basic-authentication)
    - [Example](#example)
    - [Unauthenticated request error](#unauthenticated-request-error)
    - [The support for HTTP basic authentication in the Zowe REST API SDK](#the-support-for-http-basic-authentication-in-the-zowe-rest-api-sdk)
  - [Token-based Authentication](#token-based-authentication)
    - [Token-based Login Flow and Request/Response Format](#token-based-login-flow-and-requestresponse-format)
      - [Obtaining the token](#obtaining-the-token)
      - [Authenticated request](#authenticated-request)
    - [Token format](#token-format)
    - [Validating tokens](#validating-tokens)
    - [The support for token-based authentication in the Zowe REST API SDK](#the-support-for-token-based-authentication-in-the-zowe-rest-api-sdk)

This document describe how the API client can authenticate to a Zowe API service and what the Zowe REST API SDK provides in order to achieve it.

*Authentication* is the process of verifying that "you are who you say you are". In the case of the Zowe APIs it the mainframe user ID.
The API client needs to provide information than can be verified by the API service and proves that the client has the valid credentials.

This can be done in two ways for API services in the Zowe ecosystem:

1. HTTP basic authentication with the user ID and password/passphrase
2. A JSON Web Token (JWT) token that proves that its owner is who she/he claims to be

## HTTP Basic Authentication

HTTP basic is the standard authentication as described in [HTTP authentication](https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication).

Every z/OS API service should support it and accept user ID and password for the security domain for the z/OS where z/OS API service is running. In case of an MFA setup on z/OS system, the password that contains the second factor will be different for each request.

The API service may accept PassTickets instead of password if it is configured that way.

### Example

1. API client provides the HTTP basic authentication in the form of the `Authorization` header:

    ```bash
    curl -X GET "https://localhost:10080/api/v1/greeting" -H "authorization: Basic em93ZTp6b3dl"
    ```

    ```http
    GET /api/v1/greeting HTTP/1.1
    Authorization: Basic em93ZTp6b3dl
    ```

2. If the credentials are valid then the server responds with status code based on the success of the endpoint itself:

    ```http
    HTTP/1.1 200
    {
        ...
    }
    ```

### Unauthenticated request error

If the authentication fails from any reason except for internal server error then [`401`](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/401) status is returned. This status is sent with a `WWW-Authenticate` header that contains information on how to authorize correctly and contains the name of the service.

```http
GET /api/v1/greeting HTTP/1.1

HTTP/1.1 401
WWW-Authenticate: Basic realm="Zowe Sample API Service", charset="UTF-8"
```

The Zowe REST API SDK provides more details in the JSON response.
This is optional and is meant for diagnostics purposes.

### The support for HTTP basic authentication in the Zowe REST API SDK

REST API services that are developed by the Zowe REST API SDK support HTTP basic authentication by default. When it is running on z/OS then it is using the SAF interface to validate the user ID and password by the z/OS security subsystems. For testing outside of z/OS, it uses a predefined dummy credentials. For more information see [Web Security](web-security.md) and [z/OS Security](zos-security.md).

## Token-based Authentication

Since every REST request requires to provide valid authentication and REST API services do not have mechanism of a long-term web session, it is necessary to have a way how to establish an authenticated session outside of individual REST API services.

In Zowe and in many other applications, this is achieved using JWT tokens that can be obtained by a specialized serviced and then used as the authentication information. This service is described in more details at [Zowe Authentication and Authorization Service](https://github.com/zowe/api-layer/wiki/Zowe-Authentication-and-Authorization-Service).

This section focuses on the way how services in the Zowe API ecosystem are expected to accept and use these tokens so the API clients have unified experience.

### Token-based Login Flow and Request/Response Format

1. The API client obtains JWT token by using POST method on `/auth/login` endpoint of API service that requires valid user ID and password

2. The API service calls an *authentication provider* that returns a JWT token that contains the user ID claim in the HTTP cookie named `apimlAuthenticationToken` with attributes `HttpOnly` and `Secure`.

3. The API client remembers the JWT token or cookie and sends it with every request as the cookie with name `apimlAuthenticationToken`.

#### Obtaining the token

- The full URL is the base URL of the API service plus `/auth/login`. If the application has the base URL with `/api/v1` then the full URL can be: `https://hostname:port/api/v1/auth/login`.

- The credentials are provide in JSON request:

    ```json
    {
        "username": "...",
        "password": "..."
    }
    ```

- Successful login returns `204` and empty body with the token in the `apimlAuthenticationToken` cookie

- Failed authentication returns `401` without `WWW-Authenticate`

**Example**:

```bash
curl -v -c - -X POST "https://localhost:10080/api/v1/auth/login" -d "{ \"username\": \"zowe\", \"password\": \"zowe\"}"
```

```http
POST /api/v1/auth/login HTTP/1.1
Accept: application/json, */*
Content-Length: 40
Content-Type: application/json

{
    "username": "zowe",
    "password": "zowe"
}

HTTP/1.1 204
Set-Cookie: apimlAuthenticationToken=eyJhbGciOiJSUzI1NiJ9...; Path=/; Secure; HttpOnly
```

#### Authenticated request

The API client just pass the JWT token as a Cookie header with name `apimlAuthenticationToken`:

```http
GET /api/v1/greeting HTTP/1.1
Cookie: apimlAuthenticationToken=eyJhbGciOiJSUzI1NiJ9...

HTTP/1.1 200
...
```

### Token format

The JWT must contain unencrypted claims `sub`, `iat`, `exp`, `iss`, and `jti` in the meaning defined by <https://tools.ietf.org/html/rfc7519#section-4.1>. Specifically, the `sub` is the z/OS user ID and `iss` is the name of the service that issued the JWT token.

The JWT must use RS256 signature algorithm and the secret used to sign the JWT is an asymmetric key generated during installation.

**Example**:

```json
{
  "sub": "zowe",
  "iat": 1575034758,
  "exp": 1575121158,
  "iss": "Zowe Sample API Service",
  "jti": "ac2eb63e-caa6-4ccf-a527-95cb61ad1646"
}
```

### Validating tokens

API client does not need to validate the tokens, the API services must do it themselves. If the API client receives the token from another source or needs to check details in it (like user ID, expiration) then it can use `/auth/query` endpoint
that is provided by the service.

The response is a JSON response with fields `creation`, `expiration`, `userId` that correspond to `iss`, `exp`, and `sub` JWT token claims. The timestamps are in [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) format.

```bash
curl -k --cookie "apimlAuthenticationToken=eyJhbGciOiJSUzI1NiJ9..." -X GET "https://localhost:10080/api/v1/auth/query"
```

```http
GET /api/v1/auth/query HTTP/1.1
Connection: keep-alive
Cookie: apimlAuthenticationToken=eyJhbGciOiJSUzI1NiJ9...

HTTP/1.1 200
Content-Type: application/json;charset=UTF-8

{
    "userId": "zowe",
    "creation": "2019-11-29T13:39:18.000+0000",
    "expiration": "2019-11-30T13:39:18.000+0000"
}
```

### The support for token-based authentication in the Zowe REST API SDK

The Zowe REST API SDK does not support it yet but it is planned add this support exactly how it is described. The JWT tokens will be issued by configurable provider.

The JWT token provider can be:

1. Simple standalone provider that validates the credentials via `SafPlatformUser`
2. Zowe APIML provider that uses the Zowe Authentication and Authorization Service to obtain and validate JWT tokens
