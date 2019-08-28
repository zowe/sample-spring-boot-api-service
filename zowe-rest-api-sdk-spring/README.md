# Java Sample Library

[![Download](https://api.bintray.com/packages/plavjanik/zowe/java-sample-library/images/download.svg)](https://bintray.com/plavjanik/zowe/java-sample-library/_latestVersion)

Sample Java library that is published to Bintray.

## Creating new version

```bash
git tag v0.2.1
git push origin v0.2.1
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
    implementation 'net.plavjanik.sample:java-sample-library:0.2.1'
}
```
