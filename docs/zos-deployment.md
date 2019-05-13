# Deployment to z/OS

## Manual Deployment (Linux and macOS)

You can deploy the Java archive (JAR) and configuration data to a z/OS UNIX file system (zFS) and run it from the z/OS UNIX shell.

Resulting artifacts deployed to `$TARGET_DIR` on `$HOST`:

- `bin/zowe-apiservice-*-SNAPSHOT.jar`
- `config/application.yml`
- `config/keystore.p12`
- `config/truststore.p12`

Steps:

1. Change directory to the repository with the sample application:

        cd your_repository

2. Set the variables (with examples for a Zowe test system):

        export REPO=.
        export TARGET_DIR=<your target directory on z/OS>
        export HOST=<your host>
        export SSH_PORT=22
        export USERID=<your user ID>
        export VERSION=0.0.1-SNAPSHOT

3. Create remote target directory:

        ssh ${USERID}@${HOST} -p ${SSH_PORT} "mkdir -p ${TARGET_DIR}/config ${TARGET_DIR}/bin"

4. Deploy the files:

        zowe files upload file-to-uss --binary ${REPO}/build/libs/zowe-apiservice-${VERSION}.jar ${TARGET_DIR}/bin/zowe-apiservice-${VERSION}.jar
        zowe files upload file-to-uss --binary ${REPO}/config/local/keystore.p12 ${TARGET_DIR}/config/keystore.p12
        zowe files upload file-to-uss --binary ${REPO}/config/local/truststore.p12 ${TARGET_DIR}/config/truststore.p12
        zowe files upload file-to-uss --binary ${REPO}/config/local/application.yml ${TARGET_DIR}/config/application.yml

5. Modify following values in the `${TARGET_DIR}/config/application.yml`, add `zos` profile, change the port number, and change the paths to keystore and truststore:

        spring.profiles.active: https,diag,zos

        server:
            ssl:
                keyStore: config/keystore.p12
                trustStore: config/truststore.p12
            port: 10080

    *Note:* This uses the existing keystore and truststore for localhost without integration to API ML. If you want to integrate to Zowe API ML, you need to follow the instructions in [Generate a keystore and truststore for a new service on z/OS](https://zowe.github.io/docs-site/latest/extend/extend-apiml/api-mediation-security.html#zowe-runtime-on-z-os) and modify the `application.yml`.

6. Run the application in the z/OS shell:

        ssh ${USERID}@${HOST} -p ${SSH_PORT}

   In the z/OS shell start the application (assuming that your Java on your path):

        export TARGET_DIR=<your target directory on z/OS>
        export VERSION=0.0.1-SNAPSHOT
        cd ${TARGET_DIR}
        java -Xquickstart -jar bin/zowe-apiservice-${VERSION}.jar --spring.config.additional-location=file:./config/application.yml

7. The application is running you should see a log like this:

        Starting ZoweApiServiceApplication on USILCA32 with PID 17170810 (/a/plape03/zowesample1/bin/zowe-apiservice-0.0.1-SNAPSHOT.jar started by PLAPE03 in /a/plape03/zowesample1)
        The following profiles are active: https,diag,zos
        Tomcat initialized with port(s): 16080 (https)
        Tomcat started on port(s): 16080 (https) with context path ''
        Started ZoweApiServiceApplication in 25.465 seconds (JVM running for 27.55)

## Automated Deployment

TODO
