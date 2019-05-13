# Integrate with Zowe API Mediation Layer

The default external configuration file for running on workstation [`config/local/application.yml`](/config/local/application.yml) has the integration for API Mediation Layer disabled.

## Steps to Enable Integration

1. Get access to existing instance of Zowe API Mediation layer or install your instance on your workstation from the [zowe/api-layer](https://github.com/zowe/api-layer/) repository and following the instructions in the [README.md](https://github.com/zowe/api-layer/blob/master/README.md).
2. Update following settings:
   * Set `apiml.enabled` to `true`
   * Change the `apiml.service.serviceId` to a unique service ID
   * Set `apiml.service.hostname` and `apiml.service.ipAddress` to the hostname and the IP address of your server. You can keep `localhost` for development purposes if your API Mediation Layer is running also running on the same server
   * Set `api.service.discoveryServiceUrls` to the URL of the API Mediation Layer Discovery Service. You can keep `https://localhost:10011/eureka` if your API Mediation Layer is running also running on the same server and using the default port `10011` for the Discovery Service

Example of setting these values in [`config/local/application.yml`](/config/local/application.yml):

```yaml
apiml:
    enabled: true
    service:
        serviceId: zowesample
        hostname: localhost
        ipAddress: 127.0.0.1
        discoveryServiceUrls:
            - https://localhost:10011/eureka
```

Example of setting these value on command line:

    ./gradlew bootRun --args='--spring.config.additional-location=file:./config/local/application.yml --apiml.enabled=true --apiml.service.serviceId=zowesample --apiml.service.hostname=localhost --apiml.service.ipAddress=127.0.0.1 --apiml.service.discoveryServiceUrls=https://localhost:10011/eureka'

## Setting API Service Metadata

As a developer of your API service, you need to provide metadata for the correct registration.

The sample has correct settings in [`application.yml`](/src/main/resources/application.yml) under key `apiml.service`.

It has two sections:

1. Provides default values that can be changed by user:

    ```yaml
    serviceId: zowesample
    title: Zowe Sample API Service
    description: Sample Spring Boot API service that provides REST API
    catalogUiTile:
        id: sample
        title: Sample API Services
        description: Sample API services to demonstrate exposing a REST API service in the Zowe ecosystem
        version: 1.0.0
    ```

2. Defines the API service, routing, and APIs:

    ```yaml
    baseUrl: ${apiml.service.scheme}://${apiml.service.hostname}:${server.port}/
    homePageRelativeUrl:
    statusPageRelativeUrl: actuator/info
    healthCheckRelativeUrl: actuator/health
    routes:
        - gatewayUrl: api/v1
            serviceUrl: /api/v1
    apiInfo:
        - apiId: org.zowe.sample.api
            gatewayUrl: api/v1
            version: 1.0.0
            title: Zowe Sample REST API
            description: Sample Spring Boot REST API for Zowe
            swaggerUrl: ${apiml.service.scheme}://${apiml.service.hostname}:${server.port}/api/v1/apiDocs
    ```

The description of the properties are in the Zowe documentation.

## Resources

* [Zowe Docs - Developing for API Mediation Layer - Java REST APIs service without Spring Boot](https://zowe.github.io/docs-site/latest/extend/extend-apiml/api-mediation-onboard-an-existing-java-rest-api-service-without-spring-boot-with-zowe-api-mediation-layer.html)
* [Setting Up Swagger 2 with a Spring REST API](https://www.baeldung.com/swagger-2-documentation-for-spring-rest-api)
