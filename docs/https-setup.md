# Setup HTTPS and Create TLS/SSL Certificates

## Setup for localhost

The default external configuration file for running on workstation [`config/local/application.yml`](/config/local/application.yml) uses the `https` profile.

It uses the `keystore.p12` and `truststore.p12` in [`config/local/`](/config/local/).

The sample keystore and truststore are provided in the repository.

Following commands generated them:

    $ZOWE_HOME/api-layer/scripts/apiml_cm.sh --action new-service --service-alias localhost --service-ext "SAN=dns:localhost.localdomain,dns:localhost" \
    --service-keystore config/local/keystore \
    --service-truststore config/local/truststore \
    --service-dname "CN=Zowe Sample API Service, OU=Sample, O=Zowe, L=Prague, S=Prague, C=CZ" \
    --service-password password --service-validity 3650 \
    --local-ca-filename $ZOWE_HOME/api-layer/keystore/local_ca/localca \
    --local-ca-password local_ca_password

**Warning**: Do not use this certificate in production. It is only for development purposes.

## TLS Configuration

The [`application.yml`](/src/main/resources/application.yml) contains the default settings for the TLS protocol:

- Allow TLS 1.2 only
- Allow specific ciphers (ciphers from https://wiki.mozilla.org/Security/Server_Side_TLS#Modern_compatibility + `TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA`, `TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA` that are available on ZD&T machines and Google Chrome)
- Settings were tested by <https://github.com/drwetter/testssl.sh>:

      docker run -ti drwetter/testssl.sh https://<your hostname>:10080/api/v1/greeting

## Additional Documentation

For more information about the Zowe certificate management refer to:
<https://zowe.github.io/docs-site/latest/extend/extend-apiml/api-mediation-security.html>
