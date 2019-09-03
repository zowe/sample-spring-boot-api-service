# [Zowe](https://zowe.org/) Sample API Service

Use this sample Spring Boot REST API as a starting point to create a new z/OS-based microservice that:

- Supports basic auth via SAF validation (when running on z/OS)
- Can invoke traditional z/OS services via assembler or metal C interface through JNI
- Is automatically discoverable via the [Zowe API Mediation Layer](https://github.com/zowe/api-layer)

## Prerequisites

Following platform is required to run the application:

- Java JDK 8
  - For example: <https://adoptopenjdk.net/releases.html?variant=openjdk8&jvmVariant=openj9>
  - Set `JAVA_HOME` system variable with a valid JDK path and add `${JAVA_HOME}/bin` to the PATH variable

- Zowe CLI (optional)
  - For example: <https://www.npmjs.com/package/@zowe/cli>

## Quick Start

Download the source code of the sample from <https://github.com/zowe/sample-spring-boot-api-service/releases/latest/download/zowe-rest-api-sample-spring.zip> and unzip it:

```bash
curl -OL https://github.com/zowe/sample-spring-boot-api-service/releases/latest/download/zowe-rest-api-sample-spring.zip
unzip zowe-rest-api-sample-spring.zip
cd zowe-rest-api-sample-spring
```

Build you application:

```bash
./gradlew build
```

**Note:** On Windows use `gradlew` instead of `./gradlew`

Start your application:

```bash
./gradlew bootRun
```

## Use the Application

Open <https://localhost:10080/api/v1/greeting> in your browser or favorite REST API client (for example [HTTPie](https://httpie.org/), [REST Client for VSCode](https://marketplace.visualstudio.com/items?itemName=humao.rest-client), or [Insomnia](https://insomnia.rest/).

Use `zowe` as the username and `zowe` as the password.

The default page <https://localhost:10080/> opens Swagger UI with the API Documentation.

![Swagger UI](docs/images/swagger.png)

## Customize and Extend Your Application

For further reference, please consider the following sections:

- [External Configuration](docs/config.md)
- [Logging](docs/logging.md)
- [Setup HTTPS and Create TLS/SSL Certificates](docs/https-setup.md)
- [Integrate with Zowe API Mediation Layer](docs/zowe-integrate-with-apiml.md)
- [Web Security](docs/web-security.md)
- [Deployment to z/OS](docs/zos-deployment.md)
- [z/OS Security](docs/zos-security.md)
- [z/OS Native OS Linkage](docs/zos-native-os-linkage.md)
- [Error Handling](docs/error-handling.md)

## Learn More about Gradle and Spring Boot

### Reference Documentation

For further reference, please consider the following sections:

- [Official Gradle documentation](https://docs.gradle.org)

### Guides

The following guides illustrate how to use some features concretely:

- [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
- [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
- [Building REST services with Spring](https://spring.io/guides/tutorials/bookmarks/)
- [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
- [Setting Up Swagger 2 with a Spring REST API](https://www.baeldung.com/swagger-2-documentation-for-spring-rest-api)
- [Spring Security Basic Authentication](https://www.baeldung.com/spring-security-basic-authentication)
- [Spring Security Authentication Provider](https://www.baeldung.com/spring-security-authentication-provider)

### Additional Links

These additional references should also help you:

- [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)
