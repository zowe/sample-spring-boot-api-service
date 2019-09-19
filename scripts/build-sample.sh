#!/usr/bin/env bash
./gradlew :zowe-rest-api-commons-spring:saveVersion :zowe-rest-api-commons-spring:publishToMavenLocal
VERSION=$(cat zowe-rest-api-commons-spring/.version)
echo VERSION=$VERSION
cd zowe-rest-api-sample-spring
RE="org\.zowe:zowe-rest-api-commons-spring:[0-9]\.[0-9]\.[0-9]\(-SNAPSHOT\)\{0,1\}"
RE2="version = '[0-9]\.[0-9]\.[0-9]\(-SNAPSHOT\)\{0,1\}'"
echo "Previous version:"
grep "$RE" build.gradle
cp build.gradle build.gradle-orig
cat build.gradle-orig | sed "s|$RE|org.zowe:zowe-rest-api-commons-spring:$VERSION|g" > build.gradle
echo "New version:"
grep "$RE" build.gradle
./gradlew build
