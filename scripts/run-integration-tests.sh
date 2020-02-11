#!/usr/bin/env bash
set -e

cd zowe-rest-api-sample-spring
java -jar build/libs/zowe-rest-api-sample-spring-*.jar --spring.config.additional-location=file:./config/local/application.yml &
PID=$!
echo $PID > .pid

TEST_USERID=zowe2 TEST_PASSWORD=zowe TEST_PORT=10080 TEST_WAIT_MINUTES=1 ./gradlew integrationTest
TEST_RESULT=$?
cd ..

PID=$(cat zowe-rest-api-sample-spring/.pid)
rm zowe-rest-api-sample-spring/.pid
echo "Killing process $PID"
kill -9 $PID
exit $TEST_RESULT
