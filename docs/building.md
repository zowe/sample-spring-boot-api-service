# Building SDK

This page provides instruction how to build subprojects of the SDK. This is intended for SDK developers. If you need to build a new API service, follow instructions at [zowe-rest-api-sample-spring/README.md](../zowe-rest-api-sample-spring/README.md).

## Java code

### All Projects

In the repository root directory:

```bash
./gradlew build
```

This builds both `zowe-rest-api-commons-spring` and `zowe-rest-api-sample-spring`. `zowe-rest-api-sample-spring` includes `zowe-rest-api-commons-spring` as a Gradle subproject.

### Individually

```bash
cd zowe-rest-api-commons-spring
./gradlew publishToMavenLocal
```

This places the artifacts of `zowe-rest-api-commons-spring` library to local Maven repository
with a hardcoded version `0.0.0-SNAPSHOT` that will be used by the sample project.

```bash
cd ../zowe-rest-api-sample-spring
./gradlew build
```

## CLI Tool

See [zowe-api-dev/README.md](zowe-api-dev/README.md).

## z/OS Native Code

- [Building z/OS native code](docs/zos-native-code.md)
