#!/usr/bin/env bash
./gradlew clean
rm -rf zowe-rest-api-sample-spring/.gradle
rm -rf zowe-rest-api-sample-spring/bin
rm -rf zowe-rest-api-sample-spring/build
rm -f zowe-rest-api-sample-spring/lastJob.json
rm -f zowe-rest-api-sample-spring/user-zowe-api.json
rm -f zowe-rest-api-sample-spring/build.gradle-orig
mkdir -p build
rm -f build/zowe-rest-api-sample-spring.zip
zip -r build/zowe-rest-api-sample-spring.zip zowe-rest-api-sample-spring
ls -l build/zowe-rest-api-sample-spring.zip
