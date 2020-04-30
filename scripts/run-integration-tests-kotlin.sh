#!/usr/bin/env bash
set -e

echo 'starting tests'
cd zowe-rest-api-sample-kotlin-spring
java -jar build/libs/zowe-rest-api-sample-kotlin-spring-*.jar --spring.config.additional-location=file:./config/local/local.yml &
PID=$!
echo $PID > .pid

TEST_USERID=zowe2 TEST_PASSWORD=zowe TEST_PORT=10090 TEST_WAIT_MINUTES=1 ./gradlew integrationTest
TEST_RESULT=$?
cd ..

PID=$(cat zowe-rest-api-sample-kotlin-spring/.pid)
rm zowe-rest-api-sample-kotlin-spring/.pid
echo "Killing process $PID"
kill -9 $PID
exit $TEST_RESULT
