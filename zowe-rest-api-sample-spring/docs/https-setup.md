# Setup HTTPS and Create TLS/SSL Certificates

## TrustStore and KeyStore

Location of `keystore` and `truststore` repository is specified in [`zowe-rest-api-sample-spring/config/local/application.yml`](/zowe-rest-api-sample-spring//config/local/application.yml) configuration file for example:
```yaml
server:
    address: 0.0.0.0
    port: 10080
    ssl:
        keyAlias: localhost
        keyPassword: password
        keyStore: config/local/keystore.p12
        keyStorePassword: password
        keyStoreType: PKCS12
        trustStore: config/local/truststore.p12
        trustStorePassword: password
        trustStoreType: PKCS12
```
### Content of TrustStore
When your service establishes a secure connection to another server (eg. your service calls other REST API), certificate of that server is validated by your service first. This validation requires a truststore repository that must contain certificates or root certificate authorities (CA) of servers which your service communicates with.  

Command to create a truststore `truststore.p12` repository and import a certificate `ca.cer`.
```sh
keytool -importcert -keystore truststore.p12 -trustcacerts -alias rootca -storepass password -file ca.cer -storetype PKCS12 --noprompt
```

**NOTE:** If your service does not initiate secure connections (eg. calling REST API of another service), then truststore is not needed.

### Content of KeyStore
Keystore repository of your service contains a private key and chain of certificates, that server provides to a client who can validate that communication between server and client was not tampered.

Keystore has to contain at least one record with alias matching `keyAlias` parameter. This record has to contain:
 private key and associated certificate and optionally chain of [intermediate certificate authorities](https://en.wikipedia.org/wiki/Public_key_certificate#Intermediate_certificate) if you don't use a self-signed certificate.
 
**NOTE**: a root certificate is not needed in the keystore, because clients should have  the root certificate authorities in their truststore.

## Setup for localhost

The default external configuration file for running on workstation [`zowe-rest-api-sample-spring/config/local/application.yml`](/zowe-rest-api-sample-spring//config/local/application.yml) uses the `https` profile.

It uses the `keystore.p12` and `truststore.p12` in [`zowe-rest-api-sample-spring/config/local/`](/zowe-rest-api-sample-spring/config/local/).

The sample keystore and truststore are provided in the repository.

Following commands generated them:

    $ZOWE_HOME/api-layer/scripts/apiml_cm.sh --action new-service --service-alias localhost --service-ext "SAN=dns:localhost.localdomain,dns:localhost,dns:host.docker.internal" \
    --service-keystore config/local/keystore \
    --service-truststore config/local/truststore \
    --service-dname "CN=Zowe Sample API Service, OU=Sample, O=Zowe, L=Prague, S=Prague, C=CZ" \
    --service-password password --service-validity 3650 \
    --local-ca-filename $ZOWE_HOME/api-layer/keystore/local_ca/localca \
    --local-ca-password local_ca_password

**Warning**: Do not use this certificate in production. It is only for development purposes.

**Note:** The public root CA certificates were imported to `config/localhost/truststore.p12` by:

    ```bash
    keytool -importkeystore -srckeystore $JAVA_HOME/jre/lib/security/cacerts -destkeystore config/local/truststore.p12 -srcstoretype JKS -deststoretype PKCS12 -deststorepass password --srcstorepass changeit
    ```

## TLS Configuration

The [`application.yml`](/zowe-rest-api-sample-spring/src/main/resources/application.yml) contains the default settings for the TLS protocol:

- Allow TLS 1.2 only
- Allow specific ciphers (ciphers from https://wiki.mozilla.org/Security/Server_Side_TLS#Modern_compatibility + `TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA`, `TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA` that are available on ZD&T machines and Google Chrome)
- Settings were tested by <https://github.com/drwetter/testssl.sh>:

      docker run -ti drwetter/testssl.sh https://<your hostname>:10080/api/v1/greeting

## Additional Documentation

For more information about the Zowe certificate management refer to:
<https://docs.zowe.org/stable/extend/extend-apiml/api-mediation-security.html>
