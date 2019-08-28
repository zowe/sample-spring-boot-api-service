# Zowe REST API SDK for Spring

[![Download](https://api.bintray.com/packages/plavjanik/zowe/zowe-rest-api-sdk-spring/images/download.svg)](https://bintray.com/plavjanik/zowe/zowe-rest-api-sdk-spring/_latestVersion)

Java library that provides useful functinality for z/OS REST APIs developed in Spring Boot.

## Creating new version

```bash
git tag v0.0.1
git push origin v0.0.1
```

## Publising to Maven Local

`./gradlew publishToMavenLocal`

## Publishing to Bintray

You need to provide your credentials to Bintray. You can get it from <https://bintray.com/profile/edit>, section `API Key`.

`BINTRAY_USER=user BINTRAY_API_KEY=apikey ./gradlew uploadBintray`

## Using the library

```gradle
plugins {
    id 'java'
}

repositories {
    mavenLocal()
    jcenter()
}

dependencies {
    implementation 'org.zowe:zowe-rest-api-sdk-spring:0.0.1'
}
```
