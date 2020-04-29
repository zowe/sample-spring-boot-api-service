# Zowe Kotlin Sample API Service

Use this sample Spring Boot REST API written in Kotlin as a starting point to create a new z/OS-based service. It
demonstrates only the basic functionality with the one sample controller, https configuration, and Zowe API ML
integration. For more advanced features look at [JAVA Sample API Service](zowe-rest-api-sample-spring).

## Spring Profiles

The Sample REST API runs in HTTP by default. Use the following Spring profiles to activate additional features.

  * https - activates HTTPS
  * apiml - activates integration with the Zowe API Mediation Layer

## Build

Run `gradlew build`

## Execution

Run `gradlew bootRun`

It loads the configuration from
[zowe-rest-api-sample-kotlin-spring/config/local/local.yml](zowe-rest-api-sample-kotlin-spring/config/local/local.yml).
Modify parameters or use a different configuration file for your service.

## Integration tests

Run `gradlew integrationTest`

## Mainframe deployment

Follow the steps to deploy and run your instance on the mainframe. Check
[Zowe API Development CLI Tool](https://github.com/zowe/sample-spring-boot-api-service/blob/master/zowe-rest-api-sample-spring/docs/devtool.md)
for more details about prerequisites, each step, and additional deployment options.

**Prerequisites:**

  * Zowe CLI (including configured profile)
  * Zowe API Development CLI Tool

**Instructions:**

1. Change the project directory

    ```bash
    cd zowe-rest-api-sample-kotlin-spring
    ```

2. Initiate the user-specific configuration

     ```bash
     zowe-api-dev init --account={account} --zosHlq={HLQ}.ZOWE.KOTLIN.SAMPLE --zosTargetDir=/a/{userId}}/sample/kotlin
     ```

3. Allocate a ZFS filesystem

     ```bash
     zowe-api-dev zfs
     ```

4. Build your application

     ```bash
     gradlew build
     ```

5. Deploy jar files to the mainframe

     ```bash
     zowe-api-dev deploy
     ```

5. Configure the deployed application

     ```bash
     zowe-api-dev config --name zos --parameter port=10090
     ```

6. Start your application on the mainframe

     ```bash
     zowe-api-dev start
     ```
