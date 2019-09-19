#!/usr/bin/env bash
eval MY_ZOS_HLQ=$ZOS_HLQ
eval MY_ZOS_TARGET_DIR=$ZOS_TARGET_DIR
env
zowe profiles create zosmf-profile $ZOWE_CLI_PROFILE_NAME --host $ZOS_HOST --port $ZOS_ZOSMF_PORT --user $ZOS_USERID --pass "$ZOS_PASSWORD" --reject-unauthorized false --overwrite
zowe profiles create ssh-profile $ZOWE_CLI_PROFILE_NAME --host river.zowe.org --port $ZOS_SSH_PORT --user $ZOS_USERID --password "$ZOS_PASSWORD" --overwrite
zowe profiles set-default zosmf $ZOWE_CLI_PROFILE_NAME
zowe profiles set-default ssh $ZOWE_CLI_PROFILE_NAME
cd zowe-rest-api-commons-spring
zowe-api-dev init --zosTargetDir $MY_ZOS_TARGET_DIR/zowe-rest-api-sample-spring --zosHlq $MY_ZOS_HLQ.SAMPLE --account $ZOS_ACCOUNT_NUMBER --force
zowe-api-dev zfs -p "$ZOS_ZFSADM_PARAM"
zowe-api-dev zosbuild
zowe-api-dev zfs --unmount --delete
cd ..
cd zowe-rest-api-sample-spring
zowe-api-dev init --zosTargetDir $MY_ZOS_TARGET_DIR/zowe-rest-api-commons-spring --zosHlq $MY_ZOS_HLQ.COMMONS --account $ZOS_ACCOUNT_NUMBER --force
zowe-api-dev zfs -p "$ZOS_ZFSADM_PARAM"
zowe-api-dev zosbuild
zowe-api-dev zfs --unmount --delete
