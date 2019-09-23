# Building z/OS Native Code

Both subprojects provide documentation on how to build the z/OS native code:

- [zowe-rest-api-sample-spring](/zowe-rest-api-sample-spring/docs/zos-native-os-linkage.md)
- [zowe-rest-api-commons-spring](/zowe-rest-api-commons-spring/docs/zos-native-code.md)

## Building in CircleCI

Native code is built on `river.zowe.org`.
The definition of the steps in CircleCI is labeled `&zosbuild` in [`.circleci/config.yml`](/.circleci/config.yml)
It calls `npm run zosbuild` that invokes [`scripts/zosbuild.sh`](.scripts/zosbuild.sh). `npm run` is used to initialize the NPM environment with Zowe CLI and zowe-api-dev modules. [`scripts/zosbuild.sh`](.scripts/zosbuild.sh) does following:

- Try to invoke Gradle tasks `zosbuild` for all subprojects. If it succeeds, it means that the build has been cached, if not scripts needs to continue
- Initialize Zowe profiles
- Initialize zFS filesystem for all Zowe builds if necessary (its size is defined in [zowe-api.json](/zowe-api.json))
- Execute z/OS builds for subprojects
- Delete build work files

### Required Environment Variables

Everything except credentials (`ZOS_USERID`, `ZOS_PASSWORD`, `TEST_USERID`, `TEST_PASSWORD`) is stored in [`.circleci/river.env`](/.circleci/river.env).
This file is read by `zosbuild` step in [`.circleci/config.yml`](/.circleci/config.yml).
Credentials are set in <https://circleci.com/gh/zowe/sample-spring-boot-api-service/edit#env-vars>.
