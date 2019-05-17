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

## Troubleshooting

There are several errors that can occur during reporting:

* **ERROR ZWEAS001E Unable to connect to Zowe API Mediation Layer. The certificate of the service is not trusted by the API Mediation Layer**

  * *Reason*: The certificate of the service in the keystore that is define by `server.ssl.keystore*` properties is not trusted by API Mediation Layer so the registration and updates of the service cannot be completed.

  * *Action*: There are following ways how to establish trust of the service by the API Mediation Layer:
    1. Import the service certificate or the signing CA certificate into the API ML truststore. The procedure is documented at [Add a service with an existing certificate to API ML on z/OS](https://zowe.github.io/docs-site/latest/extend/extend-apiml/api-mediation-security.html#zowe-runtime-on-z-os).
    2. Generate a new certificate for the service using API ML Certificate Management. The procedure is documented at [Generate a keystore and truststore for a new service on z/OS](https://zowe.github.io/docs-site/latest/extend/extend-apiml/api-mediation-security.html#zowe-runtime-on-z-os).

* **ERROR ZWEAS002E Unable to connect to Zowe API Mediation Layer and register or update the service information: _message_**

  * *Reason*: The service could not connect to the Discovery Service in the API Mediation Layer from the reason that is mentioned in the _message_.

  * *Action*: Review the _message_ and verify if the URLs of the Discovery Service in the `apiml.service.discoveryServiceUrls` are correct. They need to be in the form of: `https://hostname:port/eureka`. Check the hostname, port, and if the API Mediation Layer is started up.

* **ERROR ZWEAS003E Unable to connect to Zowe API Mediation Layer. The certificate of API Mediation Layer is not trusted by the service: javax.net.ssl.SSLHandshakeException: PKIX path building failed, unable to find valid certification path to requested target**

  * *Reason*: The certificate of the API Mediation Layer is not trusted by the service and its truststore that is defined by `server.ssl.truststore*` properties so the registration and updates of the service cannot be completed.

  * *Action*: Import the APIML server public certificate to the truststore of your service. By default, API ML creates a local CA. Import the API ML local CA public certificate to the truststore of the service.

    The public certificate in the [PEM format](https://en.wikipedia.org/wiki/Privacy-Enhanced_Mail) is stored at `$ZOWE_ROOT_DIR/api-mediation/keystore/local_ca/localca.cer` where `$ZOWE_ROOT_DIR` is the directory that was used for the Zowe runtime during installation.

    The certificate is stored in UTF-8 encoding so you need to transfer it as a binary file. Since this is the certificate that the service is going to trust, it is recommended to use a secure connection for transfer.

    **Follow these steps:**

    1. Download the local CA certificate to your computer. Use one of the following methods to download the local CA certificate to your computer:

        * Use [Zowe CLI](https://github.com/zowe/zowe-cli#zowe-cli--)
        Issue teh following command:

        ```bash
        zowe zos-files download uss-file --binary $ZOWE_ROOT_DIR/api-mediation/keystore/local_ca/localca.cer`
        ```

        * **Use `sftp`**
        Issue the following command:

        ```bash
        sftp <system>
        get $ZOWE_ROOT_DIR/api-mediation/keystore/local_ca/localca.cer
        ```

        To verify that the file has been transferred correctly, open the file. The following heading and closing should appear:

        ```txt
        -----BEGIN CERTIFICATE-----
        ...
        -----END CERTIFICATE-----
        ```

    2. Import the certificate to your root certificate store and trust it. If the service has a truststore in file `config/truststore.p12` then the command to import the API ML local CA public certificate.

        ```bash
        keytool -importcert -trustcacerts -noprompt -file localca.cer -alias apimlca -keystore config/truststore.p12 -storepass <store-password> -storetype PKCS12
        ```
