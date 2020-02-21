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
zowe profiles create ssh-profile $ZOWE_CLI_PROFILE_NAME --host $ZOS_HOST --port $ZOS_SSH_PORT --user $ZOS_USERID --password "$ZOS_PASSWORD" --overwrite > /dev/null
zowe profiles set-default ssh $ZOWE_CLI_PROFILE_NAME

echo "Removing $ZOS_TARGET_DIR/b$CIRCLE_BUILD_NUM"
zowe zos-uss issue ssh "rm -rf $ZOS_TARGET_DIR/b$CIRCLE_BUILD_NUM"
