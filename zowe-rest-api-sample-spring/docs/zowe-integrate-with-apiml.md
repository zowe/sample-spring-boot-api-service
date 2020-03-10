# Integrate with Zowe API Mediation Layer

The default external configuration file for running on workstation [`zowe-rest-api-sample-spring/config/local/application.yml`](/zowe-rest-api-sample-spring/config/local/application.yml) has the integration for API Mediation Layer disabled.

## Steps to Enable Integration

1. Get access to existing instance of Zowe API Mediation layer or install your instance on your workstation from the [zowe/api-layer](https://github.com/zowe/api-layer/) repository and following the instructions in the [README.md](https://github.com/zowe/api-layer/blob/master/README.md).
2. Update following settings:
   * Set `apiml.enabled` to `true`
   * Change the `apiml.service.serviceId` to a unique service ID
   * Set `apiml.service.hostname` and `apiml.service.ipAddress` to the hostname and the IP address of your server. You can keep `localhost` for development purposes if your API Mediation Layer is running also running on the same server
   * Set `api.service.discoveryServiceUrls` to the URL of the API Mediation Layer Discovery Service. You can keep `https://localhost:10011/eureka` if your API Mediation Layer is running also running on the same server and using the default port `10011` for the Discovery Service
   
    **Note**: Certificates in keystore and truststore, that establish a trust between Zowe API Mediation layer and Service, are already set correctly because both Zowe API Mediation layer and Service run on localhost hence they use keystore and truststore from git repository with pre-populated certificates. **This setup is not for production use!!!** If you have a temptation to use it in production you open door for [MITM attack](https://en.wikipedia.org/wiki/Man-in-the-middle_attack) on the communication between your Service and Zowe API Mediation layer. For further details look at section [How to Set Certificates for Zowe Service](#How-to-set-certificates-for-Zowe-Service).


Example of setting these values in [`zowe-rest-api-sample-spring/config/local/application.yml`](/zowe-rest-api-sample-spring/config/local/application.yml):

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

The sample has correct settings in [`application.yml`](/zowe-rest-api-sample-spring/src/main/resources/application.yml) under key `apiml.service`.

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
The certificates that were set up during this process are stored in `certs` directory (respectively `c:\zowe\certs`). We will refer to this directory as `$CERTS`. The `$CERT/server.p12` is a keystore for Zowe. Further details on the content of `$CERT` are in [Preparing certificates signed with a publicly trusted CA for your host](https://github.com/zowe/zowe-dockerfiles/tree/master/dockerfiles/zowe-1.7.1#preparing-certificates-signed-with-a-publicly-trusted-ca-for-your-host).

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

If the root CA is not a public one then you need to impor it to your truststore:

* To list the certificate chain:

    ```bash
    keytool -list -keystore $CERTS/server.p12 -storepass password --rfc
    ```

* The last certificate should be the root CA. Save the last certificate to `rootca.pem` and then import it to the truststore:

    ```bash
    keytool -importcert -keystore config/zowedocker/truststore.p12 -trustcacerts -alias rootca -storepass password -file rootca.pem -storetype PKCS12 --noprompt
    ```

Set `hostname` to `host.docker.internal` for the service when registering so the Zowe in Docker can see the service that is running on the docker host. In `discoveryServiceUrls` use hostname of the Zowe docker container (eg. myhost.acme.net).

Either you can override all the parameters on command line when starting sample service 
```bash
./gradlew bootRun --args='--spring.config.additional-location=file:./config/local/application.yml \
    --apiml.enabled=true --apiml.service.serviceId=zowesample --apiml.service.hostname=host.docker.internal \
    --apiml.service.ipAddress=127.0.0.1 --apiml.service.discoveryServiceUrls=https://myhost.acme.net:7553/eureka'
```
or you can make a permanent change  in [`zowe-rest-api-sample-spring/config/local/application.yml`](/zowe-rest-api-sample-spring/config/local/application.yml):

```yaml
apiml:
    enabled: true
    service:
        serviceId: zowesample
        hostname: host.docker.internal
        discoveryServiceUrls:
            - https://myhost.acme.net:7553/eureka
```
and start the sample service as usual using:

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

## How to Set Certificates for Zowe Service

This section describes how to obtain certificates for Zowe Service or how to establish trust between Zowe API Mediation layer and Service. For further information look at [Certificate management in Zowe API Mediation Layer
](https://docs.zowe.org/stable/extend/extend-apiml/api-mediation-security.html#certificate-management-in-zowe-api-mediation-layer). 


You need to specify `keyStore` and `trustStore` parameter in configuration of your service [`zowe-rest-api-sample-spring/config/local/application.yml`](/zowe-rest-api-sample-spring/config/local/application.yml). For further details on keystore and truststore look at [TrustStore and KeyStore](/zowe-rest-api-sample-spring/docs/https-setup.md#TrustStore-and-KeyStore) section. 


### Content of TrustStore with Respect to Zowe API Mediation Layer
Truststore repository of your service contains certificates or root certificate authorities (CA) of servers which service is communicating with. Because service registers itself to Zowe API Mediation Layer via Discovery service at least Discovery service's certificate in your service truststore is needed so our service can validate that communication was not tampered.

Moreover the certificate of Discovery service is signed with Zowe internal CA, therefore we can put either Zowe internal CA in truststore or a certificate of Discovery service. If Zowe internal CA is presented in your service truststore, no further certificates of other services, that your service call, have to be added to truststore in future, provided they all are signed by Zowe internal CA.

If Discovery service certificate is signed by a public CA even then a root certificate of public CA has to be imported into truststore because `trustStore` parameter overrides default Java truststore `cacerts` that contains public CA.

The bottom line the truststore has to contain at least record with certificate of Discovery service or a root certificate.

Command to create a truststore `truststore.p12` repository and import a certificate `ca.cer`.
```sh
keytool -importcert -keystore truststore.p12 -trustcacerts -alias rootca -storepass password -file ca.cer -storetype PKCS12 --noprompt
```
**NOTE**: Other option is to copy truststore from Zowe instance, that contains all needed certificates. Location of Zowe truststore is  `<zowe_install_dir>/1.7.1/components/api-mediation/keystore/localhost/localhost.truststore.p12` (this is path valid for Zowe 1.7.1)



There are 4 scenarios based on situation you are in. How to decide which option is right for me? So, do you have a certificate for your service?
 * Yes, I have a certificate, go for [Add a Service with an Existing Certificate to Zowe API Mediation Layer](#Add-a-Service-with-an-Existing-Certificate-to-Zowe-API-Mediation-Layer)
 * No, I don't have a certificate, don't worry there are some options left :-)
   * Does your service run on the same host like Zowe API Mediation layer and does you service have access to Zowe API Mediation layer truststore and keystore? If yes, go for [Sharing keystore with Zowe instance](#Sharing-keystore-with-Zowe-instance).
   * Can a Zowe admin generate a certificate for you on z/OS? If yes, go for [Generating Own Certificate for Your Service on z/OS](#Generating-Own-Certificate-for-Your-Service-on-z/OS).
   * Are you a paranoic person who doesn't trust anybody therefore you want to generate key pair on your machine. Welcome this is the most secure way but most complicated. For further details see [Generate a Keystore and Certificate Signing Request on Your Machine](#Generate-a-Keystore-and-Certificate-Signing-Request-on-Your-Machine).
 

### Sharing keystore with Zowe instance
In this scenario your Service needs to have access to Zowe installation folder and has to run on the same host like Zowe API Mediation layer.

Set  `keyStore` and `trustStore` parameters to `localhost.keystore.p12` and `localhost.truststore.p12` of Zowe API Mediation layer in `application.yml`. These files can be found in 
`<zowe_install_dir>/1.7.1/components/api-mediation/keystore/localhost` (this is path valid for Zowe 1.7.1)

### Add a Service with an Existing Certificate to Zowe API Mediation Layer
In this scenario you already have a signed certificate for your service and you would like to establish trust between your service and Zowe API Mediation layer. 
 1) create a new `truststore.p12` and `keystore.p12` using [instructions](/zowe-rest-api-sample-spring/docs/https-setup.md#TrustStore-and-KeyStore), update  `keyStore` and `trustStore` parameters in `application.yml`.
 1) See a section [Add a service with an existing certificate to API ML on z/OS](https://docs.zowe.org/stable/extend/extend-apiml/api-mediation-security.html#add-a-service-with-an-existing-certificate-to-api-ml-on-z-os) in Zowe documentation.


### Generating Own Certificate for Your Service on z/OS
In this scenario you don't have a certificate for your service yet and you do trust to a Zowe admin that he does not disclose a private key for your service. 

1) perform steps in a section [Generate a keystore and truststore for a new service on z/OS](https://docs.zowe.org/stable/extend/extend-apiml/api-mediation-security.html#generate-a-keystore-and-truststore-for-a-new-service-on-z-os)
2) transfer `service.keystore.p12` and `service.truststore.p12` from z/OS
3) update  `keyStore` and `trustStore` parameters in `application.yml`

### Generate a Keystore and Certificate Signing Request on Your Machine
In this scenario you don't have a certificate for your service yet and you don't want to disclose private key, therefore you want to generate your own key pair.
   1) Generate keystore
        ```sh
        keytool -genkeypair -alias myservice -keyalg RSA -keysize 2048 -keystore myservice.keystore.p12 -dname "CN=My Service, OU=dev, O=Broadcom, L=Prague, S=Prague, C=CZ" -keypass password -storepass password -storetype PKCS12  -validity 365
        ```
   1) Generate CSR
        ```sh
        keytool -certreq -alias myservice -keystore myservice.keystore.p12 -storepass password -file myservice.csr -keyalg RSA -storetype PKCS12 -dname "CN=My service, OU=dev, O=Broadcom, L=Prague, S=Prague, C=CZ" -validity 365 -ext SAN=dns:localhost.localdomain,dns:localhost,ip:1.2.3.4
        ```
        `SAN = Subject Alternative Names` - fill with all DNS, IPs used to access the service
   1) Signing CSR, you have two options
        1) Using local Zowe Certificate Authority on z/OS
            ```sh
            keytool -gencert -infile myservice.csr -outfile myservice_signed.cer -keystore local_ca/localca.keystore.p12 -alias localca -keypass password -storepass password -storetype PKCS12 -ext SAN=dns:localhost.localdomain,dns:localhost,ip:1.2.3.4 -ext KeyUsage:critical=keyEncipherment,digitalSignature,nonRepudiation,dataEncipherment -ext ExtendedKeyUsage=clientAuth,serverAuth -rfc -validity 365
            ```
            * transfer `myservice.csr` on z/OS
            * `SAN` needs to be filled with allowed ones from the request
            * `local_ca` folder can be found in `<zowe_install_dir>/1.7.1/components/api-mediation/keystore/` (this is path valid for Zowe 1.7.1)
        1) Using a 3rd party publicly trusted Certificate Authority. In this case you have to follow a process of CA
   1) create truststore repository using [Content of KeyStore](/zowe-rest-api-sample-spring/docs/https-setup.md#Content-of-KeyStore)
   1) update  `keyStore` and `trustStore` parameters in `application.yml`