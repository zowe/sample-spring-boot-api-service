#!/usr/bin/env bash

TARGET_DIR=$1

if [ -z "${TARGET_DIR}" ]; then
    echo "ERROR: Missing first argument with the target directory for keystores"
    exit 1
fi

DIRNAME=$(dirname "$0")
cp -v ${DIRNAME}/../resources/keystore.p12 $1
cp -v ${DIRNAME}/../resources/truststore.p12 $1
