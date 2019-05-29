ZOWE_INSTANCE_DIR=${ZOWE_INSTANCE_DIR:-.}
COMP_INSTANCE_DIR=${ZOWE_INSTANCE_DIR}/zowe-sample-spring-boot-service

mkdir -p ${COMP_INSTANCE_DIR}/config
${ZOWE_RUNTIME_DIR}/modules/@plavjanik/zowe-fake-cm/bin/cm.sh ${COMP_INSTANCE_DIR}/config
