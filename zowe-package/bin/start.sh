VERSION=0.0.1-SNAPSHOT
DIRNAME=$(dirname "$0")
COMP_RUNTIME_DIR=$DIRNAME/..
ZOWE_INSTANCE_DIR=${ZOWE_INSTANCE_DIR:-.}
COMP_INSTANCE_DIR=${ZOWE_INSTANCE_DIR}/zowe-sample-spring-boot-service
CONFIG_FILE=${COMP_RUNTIME_DIR}/config/application.yml
echo "COMP_RUNTIME_DIR=${COMP_RUNTIME_DIR}"
echo "ZOWE_INSTANCE_DIR=${ZOWE_INSTANCE_DIR}"
echo "COMP_INSTANCE_DIR=${COMP_INSTANCE_DIR}"
pushd ${COMP_INSTANCE_DIR}
java -jar ${COMP_RUNTIME_DIR}/bin/zowe-apiservice-${VERSION}.jar --spring.config.additional-location=file:${CONFIG_FILE}
popd
