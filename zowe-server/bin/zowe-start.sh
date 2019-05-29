#!/usr/bin/env bash

if [ -z "$1" ]; then
    echo "ERROR: Missing first argument with the instance directory"
    exit 1
fi

DIRNAME=$(dirname $0)
export ZOWE_INSTANCE_DIR=$1
export ZOWE_RUNTIME_DIR=${ZOWE_RUNTIME_DIR:-$(realpath ${DIRNAME}/..)}
mkdir -p ${ZOWE_INSTANCE_DIR}
export $(grep -v '^#' ${ZOWE_INSTANCE_DIR}/config/configuration.env | xargs)
export

${ZOWE_RUNTIME_DIR}/modules/zowe-sample-spring-boot-api-service/bin/start.sh
