#!/usr/bin/env bash
# TODO: Replace by build on z/OS in CI
mkdir -p zowe-rest-api-commons-spring/src/main/resources/lib/
curl https://gist.githubusercontent.com/plavjanik/eb09a5f74844e2d6d305f94a6885f678/raw/8ed5ba65c34e35f5ef389c45995467aee5bc9d9f/libzowe-commons-secur.so | base64 --decode > zowe-rest-api-commons-spring/src/main/resources/lib/libzowe-commons-secur.so
