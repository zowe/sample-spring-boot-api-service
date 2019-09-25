# Releasing

Releasing of the new version of SDK and sample is simple:

1. Create new release with a tag in format `vm.n.p` (e.g. `v1.2.3`) - e.g. using <https://github.com/zowe/sample-spring-boot-api-service/releases/new>

The [CircleCI build](https://circleci.com/gh/zowe/sample-spring-boot-api-service) publishes new versions of the commons library, `zowe-api-dev` tool, and creates new version of [the ZIP file with the sample](https://github.com/zowe/sample-spring-boot-api-service/releases/latest/download/zowe-rest-api-sample-spring.zip).

There is no need to change versions in the source code.

When you build all subprojects using `./gradlew build` in the repository root then the sample is using the commons library subproject. When you build the sample by `./gradlew build` in subdirectory `zowe-rest-api-sample-spring` it uses version `0.0.0-SNAPSHOT` that is expected to be at local Maven repository where it can be published by `./gradlew :zowe-rest-api-commons-spring:publishToMavenLocal`. During the CircleCI build the `0.0.0-SNAPSHOT` is replaced by the version taken from the tag.
