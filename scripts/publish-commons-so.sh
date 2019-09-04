#!/usr/bin/env bash
# TODO: Replace by build on z/OS in CI
cd zowe-rest-api-commons-spring
zowe-api-dev zosbuild
base64 src/main/resources/lib/libzowe-commons-secur.so | gist -f libzowe-commons-secur.so
