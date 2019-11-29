# API Client Authentication

This document describe how the API client can authenticate to a Zowe API service and what the Zowe REST API SDK provides in order to achieve it.

*Authentication* is the process of verifying that "you are who you say you are". In the case of the Zowe APIs it the mainframe user ID.
The API client needs to provide information than can be verified by the API service and proves that the client has the valid credentials.

This can be done in two ways for API services in the Zowe ecosystem:

1. HTTP basic authentication with the user ID and password/passphrase
2. A JWT token that proves that its owner is who she/he claims to be

## HTTP Basic Authentication

HTTP basic is the standard authentication as described in [HTTP authentication](https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication).

Every z/OS API service should support it and accept user ID and password for the security domain for the z/OS where z/OS API service is running. In case of an MFA setup on z/OS system, the password that contains the second factor will be different for each request.

The API service may accept PassTickets instead of password if it is configured that way.

## Token-based Authentication

Since every REST request requires to provide valid authentication and REST API services do not have mechanism of a long-term web session, it is necessary to have a way how to establish an authenticated session outside of individual REST API services.

In Zowe and in many other applications, this is achieved using JWT tokens that can be obtained by a specialized serviced and then used as the authentication information. This service is described in more details at [Zowe Authentication and Authorization Service](https://github.com/zowe/api-layer/wiki/Zowe-Authentication-and-Authorization-Service).

This section focuses on the way how services in the Zowe API ecosystem are expected to accept and use these tokens so the API clients have unified experience.

### Token-based Login Flow and Request/Response Format

TBD

#### Obtaining the token

TBD

#### Authenticated request

TBD

#### Unauthenticated request error

TBD
