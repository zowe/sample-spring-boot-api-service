# Error Handling in REST Controllers

- [Error Handling in REST Controllers](#error-handling-in-rest-controllers)
  - [Handling Internal Errors](#handling-internal-errors)
  - [Handling Expected Errors](#handling-expected-errors)
  - [REST API Document Format](#rest-api-document-format)
    - [Document Format](#document-format)
    - [Rules for Messages](#rules-for-messages)
    - [Defining New Numbered Message](#defining-new-numbered-message)
  - [Logging Numbered Message](#logging-numbered-message)

## Handling Internal Errors

Unexpected errors does not need to be handled or caught by your REST controller. If your controller throws an `Exception` or `RuntimeException` then Spring exception handler (customized by `CustomRestExceptionHandler` in the commons library) will convert the exception into a standardized format. For example request `https://localhost:10080/api/v1/exception` returns:

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

## Handling Expected Errors

One of the recommended ways is to use [@ControllerAdvice](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/ControllerAdvice.html) annotation.

1. Your code is detetion the error and throwing an exception:

    ```java
    if (name.trim().isEmpty()) {
        throw new EmptyNameError();
    }
    ```

    This exception can be thrown at any place, not just in the REST controller.

2. Create an exception handler that will catch and convert this exception.

    ```java
    @ControllerAdvice(assignableTypes = { GreetingController.class })
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public class GreetingControllerExceptionHandler {
        private final ErrorService errorService;

        @Autowired
        public GreetingControllerExceptionHandler(ErrorService errorService) {
            this.errorService = errorService;
        }

        @ExceptionHandler(EmptyNameError.class)
        public ResponseEntity<ApiMessage> handleEmptyName(EmptyNameError exception) {
            ApiMessage message = errorService.createApiMessage("org.zowe.sample.apiservice.greeting.empty");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON_UTF8).body(message);
        }
    }
    ```

3. Define message with key `org.zowe.sample.apiservice.greeting.empty` as described at [Logging Numbered Message](#logging-numbered-message).

You can find more guidance how to handle errors in [Error Handling for REST with Spring](https://www.baeldung.com/exception-handling-for-rest-with-spring).

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
        },
        {
            "fieldOne": "another value",
            "someNumber": 2
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
  - *messageSource* - for support and developers - source service that generated the error. The SDK returns `host:port:serviceId` by default.

Be sure to include as much useful data as possible and keep in mind different users of the message structure. However, be mindful not to leak data that should be kept private or implementation details to avoid breaches in security.

**Example:**

```json
{
  "messages": [
    {
      "messageType": "ERROR",
      "messageNumber": "ZWEAS401",
      "messageContent": "The request has not been applied because it lacks valid authentication credentials for the target resource: Full authentication is required to access this resource",
      "messageReason": "The accessed resource requires authentication. The request is missing valid authentication credentials.",
      "messageAction": "Review the product documentation for more details about acceptable authentication. Verify that your credentials are valid and contact security administrator to obtain valid credentials.",
      "messageKey": "org.zowe.commons.rest.unauthorized",
      "messageInstanceId": "d9ef6577-f66c-4845-988d-89fc3d993d72",
      "messageComponent": "org.zowe.commons.spring.RestAuthenticationEntryPoint",
      "messageSource": "usilca32.lvn.broadcom.net:10180:zowesample"
    }
  ]
}
```

### Defining New Numbered Message

1. Open [messages.yml](../src/main/resources/messages.yml)

2. Add a new message with the fields described above. For example:

    ```yml
    - key: org.zowe.sample.apiservice.greeting.empty
      number: ZWEASA001
      type: ERROR
      text: "The provided name is empty. Provide a name that is not empty."
    ```

**Notes:**

- You can use optional `reason` and `action` properties to define `messageReason` and `messageActions`.
- You can use `component` to override override the default compoment name which is the class name of the Java class that has created the message.
- The `messageSource` is set automatically by the commons library to `hostname:port:serviceId`.

## Logging Numbered Message

The `ApiMessage` interface has two methods that can be used to get the message text and use it for example in the logs or other messages other than REST API response:

- `toLogMessage()` - Returns the message number followed by the message text and message instance ID. The instance ID is useful because it can be connected to the instance ID that is returned to the user in the REST API response.

- `toReadableText()` - Return the message number followed by the message text.

**Example:**

```java
ApiMessage message = errorService.createApiMessage("message.key");
log.error(message.toLogMessage());
// Prints: "ERROR MSGNUM001E Message text {bf824f40-8031-445f-b4f5-59d7ae0c865d}"

ApiMessage message = errorService.createApiMessage("message.key");
log.info(message.toReadableText());
// Prints: "INFO MSGNUM001I Message text"
```
