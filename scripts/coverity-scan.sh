#!/usr/bin/env bash
set -e
docker run -t -d --name covo --mount type=bind,source="$PWD",target=/workspace,consistency=delegated --mount type=bind,source="$HOME/cov/root",target=/root,consistency=delegated plavjanik/coverity-oss
docker exec -it covo cov-build --fs-capture-search zowe-api-dev/src --dir cov-int ./gradlew --no-build-cache --no-daemon -x checkSharedObject -x test clean build
docker exec -it covo tar czvf cov-int.tgz cov-int
curl --form token=$COVERITY_TOKEN \
  --form email=$COVERITY_EMAIL \
  --form file=@cov-int.tgz \
  --form version="1.1.0" \
  --form description="Automated Coverity Scan" \
  "https://scan.coverity.com/builds?project=${COVERITY_PROJECT/\//%2f}"
docker rm -f covo
