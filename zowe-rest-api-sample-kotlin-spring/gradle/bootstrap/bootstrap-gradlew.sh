#!/usr/bin/env sh

if [ ! -f gradle/wrapper/gradle-wrapper.jar ]; then
    echo "Gradle wrapper not found. Attempting to download..."
    curl --silent --output gradle/wrapper/gradle-wrapper.jar \
            https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar
    rc=$?;
    if [ $rc != 0 ]; then
        echo "Gradle wrapper download failed. Bootstrap failed."
        exit 1
    else
        echo "Gradle wrapper download success. Bootstrap complete."
        exit 0
    fi
else
    exit 0
fi
