#!/usr/bin/env bash
set -e

scripts/lock-port.sh

export DEBUG=*
cd zowe-rest-api-sample-spring
zowe-api-dev deploy
zowe-api-dev config --name zos -p port=$TEST_PORT
zowe-api-dev start --job --killPrevious

TEST_BASE_URI=https://$ZOS_HOST ./gradlew integrationTest
TEST_RESULT=$?

set +e
zowe-api-dev stop
cd ..

scripts/unlock-port.sh
exit $TEST_RESULT
