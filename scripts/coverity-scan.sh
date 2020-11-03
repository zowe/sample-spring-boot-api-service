#!/usr/bin/env bash
set -e
cov-build --fs-capture-search zowe-api-dev/src --dir cov-int ./gradlew --no-build-cache --no-daemon -x checkSharedObject -x test clean build
tar czvf cov-int.tgz cov-int
curl --form token=$COVERITY_TOKEN \
  --form email=$COVERITY_EMAIL \
  --form file=@cov-int.tgz \
  --form version="1.1.0" \
  --form description="Automated Coverity Scan - $GIT_BRANCH ($GIT_COMMIT)" \
  "https://scan.coverity.com/builds?project=zowe%2Fsample-spring-boot-api-service"
