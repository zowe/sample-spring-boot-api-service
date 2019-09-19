#!/usr/bin/env bash
mkdir -p test_libs
java -cp zowe-rest-api-sample-spring/build/libs/zowe-rest-api-sample-spring-*.jar -Dloader.main=org.zowe.sample.apiservice.LibsExtractor org.springframework.boot.loader.PropertiesLauncher test_libs
test -f test_libs/libzowe-sample.so && test -f test_libs/libzowe-commons-secur.so
echo "Result: $?"
