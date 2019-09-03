# Error Handling in REST Controllers

## Handling Errors

Unexpected errors does not need to be handled or catched by your REST controller. If your controller throws an `Exception` or `RuntimeException` then Spring exception handler (customized by `CustomRestExceptionHandler` in the commons library) will convert the exception into a standardized format. For example request `https://localhost:10080/api/v1/exception` returns:

```json
{
    "messages": [
        {
            "messageContent": "The service has encountered a situation it doesn't know how to handle. Please contact support for further assistance. More details are available in the log under message instance ID: 9912494f-6c02-49e4-a6af-a46040d0890d",
            "messageKey": "org.zowe.commons.rest.internalServerError",
            "messageNumber": "ZWEAS500",
            "messageType": "ERROR"
        }
    ]
}
```

More examples and explanations how to handle and report expected errors will be added in this user story: <https://github.com/zowe/sample-spring-boot-api-service/issues/22>

## REST API Document Format

### Document Format

The response is to be in a JSON format and media type value is `application/json`. The name of the fields is to use `camelCase`. If the request is processed without any errors and warnings, then the response format is to be either a JSON object or JSON array.

Examples:

- plain data objects:

  ```json
  {
    "fieldOne": "value",
    "someNumber": 1
  }
  ```

- list of objects:

  ```json
  [
    {
        "fieldOne": "value",
        "someNumber": 1
    }
  ]
  ```

The idea is to keep the JSON simple to make it easy to process by API clients, document in OpenAPI, and make the API easy to use in tools like Swagger UI. When additional metadata are needed then HTTP response headers can be used.

If the request is processed with an error or warning, then the response is to follow the JSON format as in the following code:

```json
{
  "messages": [
    {
      "messageType": "ERROR",
      "messageNumber": "CSR0001",
      "messageContent": "Pet with id '404' is not found"
    }
  ]
}
```

The above response contains a messages section. The messages array is a place for application (or service) status and message descriptions to complement and enhance the basic HTTP status codes that are returned in the HTTP status line.

### Rules for Messages

Follow the principle that message key (for example `org.zowe.commons.apiml.serviceCertificateNotTrusted`) is for the code and message content (readable text) is for people. When an error occurs, The HTTP status codes are returned. Provide more details within this message structure. The meaning of the fields is (in order of importance):

- Required:
  - *messageType* - severity - `ERROR`, `WARNING`, `INFO`, `DEBUG`, or `TRACE`
  - *messageNumber* - typical mainframe message ID (not including the severity code) that can be found in CA documentation
  - *messageContent* - Readable message in US English
- Optional:
  - *messageReason* - Supplements the messageContent field, supplying more information about why the message is present.
  - *messageAction* - Recommendation of the actions to take in response to the message.
  - *messageKey* - unique message key - describing the reason for the error. It should be a dot-delimited string `tld.provider.service[.subservice].detail`. For example: `org.zowe.commons.apiml.serviceCertificateNotTrusted`. The purpose of this field is to enable UI to show a meaningful and localized error message. The message key is used instead of the message number as the number makes the code hard to read, and makes renumbering difficult and error-prone.
  - *messageParameters* - error message parameters. Used for formatting of localized messages.
  - *messageInstanceId* - unique ID of the message instance. Useful for locating of the message in the logs. The same ID should be printed in the log.
  - *messageComponent* - for support and developers - component that generated the error (can be fully qualified Java package or class name)
  - *messageSource* - for support and developers - source service that generated the error (can Open Mainframe service name or host:port).Be sure to include as much useful data as possible and keep in mind different users of the message structure. However, be mindful not to leak data that should be kept private or implementation details to avoid breaches in security.
