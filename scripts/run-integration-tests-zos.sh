#!/usr/bin/env bash
scripts/lock-port.sh

cd zowe-rest-api-sample-spring
zowe-api-dev deploy
zowe-api-dev config --name zos -p port=$TEST_PORT
zowe-api-dev start --job

TEST_BASE_URI=https://$ZOS_HOST ./gradlew integrationTest
TEST_RESULT=$?

zowe-api-dev stop
cd ..

scripts/unlock-port.sh
exit $TEST_RESULT
