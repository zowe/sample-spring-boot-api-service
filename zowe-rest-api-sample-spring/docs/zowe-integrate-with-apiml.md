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

```bash
./gradlew bootRun --args='--spring.config.additional-location=file:./config/local/application.yml
--apiml.enabled=true --apiml.service.serviceId=zowesample --apiml.service.hostname=localhost
--apiml.service.ipAddress=127.0.0.1 --apiml.service.discoveryServiceUrls=https://localhost:10011/eureka'
```

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

## Integration with the Zowe in Docker on Your Computer

This section shows how to integrate with Zowe in Docker that can be installed and started by following instructions at <https://github.com/zowe/zowe-dockerfiles/tree/master/dockerfiles/zowe-1.7.1>.

After you complete the instructions, you will have Zowe running with API Gateway listening on port `7554` and Discovery Service on port `7553`.
The certificates that were set up during this process are stored in `certs` directory (respectively `c:\zowe\certs`). We will refer to this directory as `$CERTS`.
The `server.p12` contains the server certificate and Zowe APIML truststore.

In the directory with the sample service, we issue the following command to get the local CA certificate trusted by the Zowe in Docker:

```bash
keytool -exportcert -keystore config/local/truststore.p12 -storepass password -alias localca -rfc > $CERTS/localca.cer
```
for example `keytool -exportcert -keystore config/local/truststore.p12 -storepass password -alias localca -rfc > c:\zowe\certs\localca.cer`

Then the Zowe in Docker can be started. You have to restart it, if it is running. 
```bash
docker run -it \
        -p 8544:8544 \
        -p 7552:7552 \
        -p 7553:7553 \
        -p 7554:7554 \
        -p 8545:8545 \
        -p 8547:8547 \
        -p 1443:1443 \
        --net="bridge" \
        -h myhost.acme.net \
        --env ZOWE_ZOSMF_HOST='' \
        --env ZOWE_ZOSMF_PORT=1443 \
        --env LAUNCH_COMPONENT_GROUPS=GATEWAY \
        --mount type=bind,source=c:/zowe/certs,target=/root/zowe/certs \
        --mount type=bind,source=c:/zowe/keystore,target=/root/zowe/current/components/api-mediation/keystore \
        --mount type=bind,source=c:/zowe/zowe-user-dir,target=/root/zowe-user-dir/ \
        --name "zowe" \
        --rm \
        vvvlc/zowe:latest --regenerate-certificates
```
**NOTE**: In above example replace `myhost.acme.net` with hostname of your workstation, adjust `source` value of mount parameters.

**NOTE**: In this example a fakeOSMF is used to simulate z/OSMF, if you have z/OSMF up and running you can specify `ZOWE_ZOSMF_HOST`, `ZOWE_ZOSMF_PORT` variables.

If the Zowe server certificate at `$CERTS/server.p12` is signed by a public CA such as DigiCert, then you do not need to do anything since the `config/localhost/truststore.p12` already contains them.

If the root CA is not a public one then you need to import to your truststore:

* To list the certificate chain:

    ```bash
    keytool -list -keystore $CERTS/server.p12 -storepass password --rfc
    ```

* The last certificate should be the root CA. Save the last certificate to `rootca.pem` and then import it to the truststore:

    ```bash
    keytool -importcert -keystore config/zowedocker/truststore.p12 -trustcacerts -alias rootca -storepass password -file rootca.pem -storetype PKCS12 --noprompt
    ```

We need to use `host.docker.internal` as the hostname for the service when registering so the Zowe in Docker can see the service running on the host
and use the hostname of the Zowe in Docker as the in the `discoveryServiceUrls` property.

Either you can start sample service 
```bash
./gradlew bootRun --args='--spring.config.additional-location=file:./config/local/application.yml \
    --apiml.enabled=true --apiml.service.serviceId=zowesample --apiml.service.hostname=host.docker.internal \
    --apiml.service.ipAddress=127.0.0.1 --apiml.service.discoveryServiceUrls=https://myhost.acme.net:7553/eureka'
```

Or you can make a permanet change  in [`config/local/application.yml`](/config/local/application.yml):

```yaml
apiml:
    enabled: true
    service:
        serviceId: zowesample
        hostname: host.docker.internal
        discoveryServiceUrls:
            - https://myhost.acme.net:7553/eureka
```

You can start the sample service as usual using:

```bash
java -jar build/libs/zowe-rest-api-sample-spring-*.jar --spring.config.additional-location=file:config/local/application.yml
```



## Resources

* [Zowe Docs - Developing for API Mediation Layer - Java REST APIs service without Spring Boot](https://zowe.github.io/docs-site/latest/extend/extend-apiml/api-mediation-onboard-an-existing-java-rest-api-service-without-spring-boot-with-zowe-api-mediation-layer.html)
* [Setting Up Swagger 2 with a Spring REST API](https://www.baeldung.com/swagger-2-documentation-for-spring-rest-api)

## Troubleshooting

There are several errors that can occur during reporting:

* **ERROR ZWEAS201E Unable to connect to Zowe API Mediation Layer. The certificate of the service is not trusted by the API Mediation Layer**

  * *Reason*: The certificate of the service in the keystore that is define by `server.ssl.keystore*` properties is not trusted by API Mediation Layer so the registration and updates of the service cannot be completed.

  * *Action*: There are following ways how to establish trust of the service by the API Mediation Layer:
    1. Import the service certificate or the signing CA certificate into the API ML truststore. The procedure is documented at [Add a service with an existing certificate to API ML on z/OS](https://zowe.github.io/docs-site/latest/extend/extend-apiml/api-mediation-security.html#zowe-runtime-on-z-os).
    2. Generate a new certificate for the service using API ML Certificate Management. The procedure is documented at [Generate a keystore and truststore for a new service on z/OS](https://zowe.github.io/docs-site/latest/extend/extend-apiml/api-mediation-security.html#zowe-runtime-on-z-os).

* **ERROR ZWEAS202E Unable to connect to Zowe API Mediation Layer and register or update the service information: _message_**

  * *Reason*: The service could not connect to the Discovery Service in the API Mediation Layer from the reason that is mentioned in the _message_.

  * *Action*: Review the _message_ and verify if the URLs of the Discovery Service in the `apiml.service.discoveryServiceUrls` are correct. They need to be in the form of: `https://hostname:port/eureka`. Check the hostname, port, and if the API Mediation Layer is started up.

* **ERROR ZWEAS203E Unable to connect to Zowe API Mediation Layer. The certificate of API Mediation Layer is not trusted by the service: javax.net.ssl.SSLHandshakeException: PKIX path building failed, unable to find valid certification path to requested target**

  * *Reason*: The certificate of the API Mediation Layer is not trusted by the service and its truststore that is defined by `server.ssl.truststore*` properties so the registration and updates of the service cannot be completed.

  * *Action*: Import the APIML server public certificate to the truststore of your service. By default, API ML creates a local CA. Import the API ML local CA public certificate to the truststore of the service.

    The public certificate in the [PEM format](https://en.wikipedia.org/wiki/Privacy-Enhanced_Mail) is stored at `$ZOWE_ROOT_DIR/api-mediation/keystore/local_ca/localca.cer` where `$ZOWE_ROOT_DIR` is the directory that was used for the Zowe runtime during installation.

    The certificate is stored in UTF-8 encoding so you need to transfer it as a binary file. Since this is the certificate that the service is going to trust, it is recommended to use a secure connection for transfer.

    **Follow these steps:**

    1. Download the local CA certificate to your computer. Use one of the following methods to download the local CA certificate to your computer:

        * Use [Zowe CLI](https://github.com/zowe/zowe-cli#zowe-cli--)
        Issue the following command:

        ```bash
        zowe zos-files download uss-file --binary $ZOWE_ROOT_DIR/api-mediation/keystore/local_ca/localca.cer`
        ```

        * **Use `sftp`**
        Issue the following command:

        ```bash
        sftp <system>
        get $ZOWE_ROOT_DIR/api-mediation/keystore/local_ca/localca.cer
        ```
        
        **NOTE**: If APIML server certificate is signed by a non public CA use a root certificate file that was specified on [externalCertificateAuthorities](https://docs.zowe.org/stable/user-guide/configure-zowe-runtime.html#configuration-variables) during installation.

        To verify that the file has been transferred correctly, open the file. The following heading and closing should appear:

        ```txt
        -----BEGIN CERTIFICATE-----
        ...
        -----END CERTIFICATE-----
        ```

    2. Import the certificate to your root certificate store and trust it. If the service has a truststore in file `config/local/truststore.p12` then the command to import the API ML local CA public certificate.

        ```bash
        keytool -importcert -trustcacerts -noprompt -file localca.cer -alias apimlca -keystore config/local/truststore.p12 -storepass <store-password> -storetype PKCS12
        ```
