#!/usr/bin/env bash

#
# Prototype implementation of script that configures all components - see README.md
#

if [ -z "$1" ]; then
    echo "ERROR: Missing first argument with the instance directory"
    exit 1
fi

DIRNAME=$(dirname $0)
export ZOWE_INSTANCE_DIR=$1
export ZOWE_RUNTIME_DIR=${ZOWE_RUNTIME_DIR:-$(realpath ${DIRNAME}/..)}
mkdir -p ${ZOWE_INSTANCE_DIR}/config
cp -v ${ZOWE_RUNTIME_DIR}/resources/default-configuration.env ${ZOWE_INSTANCE_DIR}/config/configuration.env
export $(grep -v '^#' ${ZOWE_INSTANCE_DIR}/config/configuration.env | xargs)

${ZOWE_RUNTIME_DIR}/modules/@plavjanik/zowe-sample-spring-boot-api-service/bin/config.sh
