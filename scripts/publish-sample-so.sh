#!/usr/bin/env bash
# TODO: Replace by build on z/OS in CI
cd zowe-rest-api-sample-spring
zowe-api-dev zosbuild
base64 src/main/resources/lib/libwtojni.so | gist -f libwtojni
