#!/usr/bin/env bash
set -e

if [ -z "$ZOS_TARGET_DIR" ]
then
    echo "\$ZOS_TARGET_DIR is not set. It needs to be set to the z/OS UNIX target directory where the z/OS native code build will be done"
    exit 1
fi

if [ -z "$ZOS_USERID" ]
then
    echo "\$ZOS_USERID is not set. It needs to be set to the user ID that is used for builds"
    exit 1
fi

if [ -z "$ZOS_PASSWORD" ]
then
    echo "\$ZOS_PASSWORD is not set. It needs to be set to the password of the '$ZOS_USERID' user ID"
    exit 1
fi

echo "Preparing Zowe CLI profiles"
zowe profiles create zosmf-profile $ZOWE_CLI_PROFILE_NAME --host $ZOS_HOST --port $ZOS_ZOSMF_PORT --user $ZOS_USERID --pass "$ZOS_PASSWORD" --reject-unauthorized false --overwrite > /dev/null
zowe profiles create ssh-profile $ZOWE_CLI_PROFILE_NAME --host $ZOS_HOST --port $ZOS_SSH_PORT --user $ZOS_USERID --password "$ZOS_PASSWORD" --overwrite > /dev/null
zowe profiles set-default zosmf $ZOWE_CLI_PROFILE_NAME
zowe profiles set-default ssh $ZOWE_CLI_PROFILE_NAME

echo "Initializing zFS"
zowe-api-dev init --zosTargetDir $ZOS_TARGET_DIR --zosHlq $ZOS_HLQ --account $ZOS_ACCOUNT_NUMBER --javaHome=$ZOS_JAVA_HOME --javaLoadlib $ZOS_JAVA_LOADLIB --force
zowe-api-dev zfs -p "$ZOS_ZFSADM_PARAM"

echo "Initializing profiles"
cd zowe-rest-api-sample-spring
zowe-api-dev init --zosTargetDir $ZOS_TARGET_DIR/b$CIRCLE_BUILD_NUM/zowe-rest-api-sample-spring --zosHlq $ZOS_HLQ.B$CIRCLE_BUILD_NUM.SAMPLE --account $ZOS_ACCOUNT_NUMBER --javaHome=$ZOS_JAVA_HOME --javaLoadlib $ZOS_JAVA_LOADLIB --force
cd ..

echo "Checking if build is needed"
set +e
./gradlew :zowe-rest-api-commons-spring:zosbuild :zowe-rest-api-sample-spring:zosbuild > /dev/null 2>&1
if [ $? -eq 0 ]; then
    exit 0
fi
set -e

cd zowe-rest-api-commons-spring
zowe-api-dev init --zosTargetDir $ZOS_TARGET_DIR/b$CIRCLE_BUILD_NUM/zowe-rest-api-commons-spring --zosHlq $ZOS_HLQ.B$CIRCLE_BUILD_NUM.COMMONS --account $ZOS_ACCOUNT_NUMBER --javaHome=$ZOS_JAVA_HOME --javaLoadlib $ZOS_JAVA_LOADLIB --force
cd ..

echo "Building native code in zowe-rest-api-commons-spring"
./gradlew :zowe-rest-api-commons-spring:zosbuild

echo "Building native code in zowe-rest-api-sample-spring"
./gradlew :zowe-rest-api-sample-spring:zosbuild
