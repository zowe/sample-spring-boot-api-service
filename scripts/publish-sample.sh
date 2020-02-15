#!/usr/bin/env bash
set -e

PLATFORM_OS=${PLATFORM_OS:-"linux"}
PLATFORM_ARCH=${PLATFORM_ARCH:-"amd64"}
TAG=${TAG:-`git describe --tags --abbrev=0`}
cd build
curl -OL https://github.com/aktau/github-release/releases/download/v0.7.2/${PLATFORM_OS}-${PLATFORM_ARCH}-github-release.tar.bz2
tar -xjvf ${PLATFORM_OS}-${PLATFORM_ARCH}-github-release.tar.bz2
bin/${PLATFORM_OS}/${PLATFORM_ARCH}/github-release info -u zowe -r sample-spring-boot-api-service
bin/${PLATFORM_OS}/${PLATFORM_ARCH}/github-release upload -u zowe -r sample-spring-boot-api-service -t ${TAG} -n "zowe-rest-api-sample-spring.zip" -f zowe-rest-api-sample-spring.zip
bin/${PLATFORM_OS}/${PLATFORM_ARCH}/github-release upload -u zowe -r sample-spring-boot-api-service -t ${TAG} -n "zowe-rest-api-sample-spring.jar" -f zowe-rest-api-sample-spring.jar
bin/${PLATFORM_OS}/${PLATFORM_ARCH}/github-release upload -u zowe -r sample-spring-boot-api-service -t ${TAG} -n "zowedev-zowe-api-dev.tgz" -f zowedev-zowe-api-dev.tgz
