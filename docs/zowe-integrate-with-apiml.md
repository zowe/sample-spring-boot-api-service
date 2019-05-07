# Integrate with Zowe API Mediation Layer

The default external configuration file for running on workstation [`config/local/application.yml`](/config/local/application.yml) has the integration for API Mediation Layer disabled.

Do following steps to enable it:

1. Get access to existing instance of Zowe API Mediation layer or install your instance on your workstation from the [zowe/api-layer](https://github.com/zowe/api-layer/) repository and following the instructions in the [README.md](https://github.com/zowe/api-layer/blob/master/README.md).
2. Update following settings:
   * Set `apiml.enabled` to `true`
   * Set `apiml.service.hostname` and `apiml.service.ipAddress` to the hostname and the IP address of your server. You can keep `localhost` for development purposes if your API Mediation Layer is running also running on the same server
   * Set `api.service.discoveryServiceUrls` to the URL of the API Mediation Layer Discovery Service. You can keep `https://localhost:10011/eureka` if your API Mediation Layer is running also running on the same server and using the default port `10011` for the Discovery Service

Example of setting these values in [`config/local/application.yml`](/config/local/application.yml):

```yaml
apiml:
    enabled: true
    service:
        hostname: localhost
        ipAddress: 127.0.0.1
        discoveryServiceUrls:
            - https://localhost:10011/eureka
```

Example of setting these value on command line:

    ./gradlew bootRun --args='--spring.config.additional-location=file:./config/local/application.yml --apiml.enabled=true --apiml.service.hostname=localhost --apiml.service.ipAddress=127.0.0.1 --apiml.service.discoveryServiceUrls=https://localhost:10011/eureka'

## Resources

* [Zowe Docs - Developing for API Mediation Layer - Java REST APIs service without Spring Boot](https://zowe.github.io/docs-site/latest/extend/extend-apiml/api-mediation-onboard-an-existing-java-rest-api-service-without-spring-boot-with-zowe-api-mediation-layer.html)
* [Setting Up Swagger 2 with a Spring REST API](https://www.baeldung.com/swagger-2-documentation-for-spring-rest-api)
