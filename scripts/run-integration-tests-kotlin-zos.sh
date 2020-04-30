#!/usr/bin/env bash
set -e

scripts/lock-port.sh

cd zowe-rest-api-sample-kotlin-spring
zowe-api-dev init --zosTargetDir $ZOS_TARGET_DIR/b$CIRCLE_BUILD_NUM/zowe-rest-api-sample-kotlin-spring --zosHlq $ZOS_HLQ.B$CIRCLE_BUILD_NUM.SAMPLE --account $ZOS_ACCOUNT_NUMBER --javaHome=$ZOS_JAVA_HOME --javaLoadlib $ZOS_JAVA_LOADLIB --force
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
