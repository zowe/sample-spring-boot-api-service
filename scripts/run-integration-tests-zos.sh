#!/usr/bin/env bash
cd zowe-rest-api-sample-spring
zowe-api-dev deploy
zowe-api-dev config --name zos -p port=$TEST_PORT
zowe-api-dev start --job

TEST_BASE_URI=https://$ZOS_HOST ./gradlew integrationTest
TEST_RESULT=$?

zowe-api-dev stop
exit $TEST_RESULT
